package com.softdesign.devintensive.ui.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.ItemUserListBinding;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.ui.fragments.UserListFragment;
import com.softdesign.devintensive.utils.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable, ItemTouchHelperAdapter {

    public interface OnItemCLickListener {
        void onItemClick(int position);
    }

    private static final EventBus BUS = EventBus.getDefault();
    private final CustomUserListFilter mFilter;
    private final OnStartDragListener mDragStartListener;

    private List<ProfileViewModel> mUsers;
    private OnItemCLickListener mViewClickListener;
    private OnItemCLickListener mLikesClickListener;
    private DatabaseOperation.Sort mSort = DatabaseOperation.Sort.CUSTOM;

    //region :::::::::::::::::::::::::::::::::::::::::: Adapter
    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);

        return new UserViewHolder(convertView, mViewClickListener, mLikesClickListener, mDragStartListener);
    }

    public UsersAdapter(List<UserEntity> users, OnStartDragListener dragStartListener) {
        mUsers = new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : users) {
                add(new ProfileViewModel(u));
            }
        }};
        mDragStartListener = dragStartListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
    }

    public UsersAdapter(List<ProfileViewModel> userEntities, UserListFragment dragStartListener, OnItemCLickListener callUserProfileFragment, OnItemCLickListener likeUser) {

        mUsers = userEntities;
        mDragStartListener = dragStartListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
        mViewClickListener = callUserProfileFragment;
        mLikesClickListener = likeUser;
    }

    public UsersAdapter(List<UserEntity> users, OnStartDragListener dragStartListener, OnItemCLickListener viewClickListener, OnItemCLickListener likesClickListener) {
        mUsers = new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : users) {
                add(new ProfileViewModel(u));
            }
        }};
        mDragStartListener = dragStartListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
        mViewClickListener = viewClickListener;
        mLikesClickListener = likesClickListener;
    }

    public void setOnViewBtnCLickListener(@Nullable OnItemCLickListener itemCLickListener) {
        mViewClickListener = itemCLickListener;
    }

    public void setOnLikeBtnCLickListener(@Nullable OnItemCLickListener itemCLickListener) {
        mLikesClickListener = itemCLickListener;
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.UserViewHolder holder, int position) {
        holder.getBinding().setProfile(mUsers.get(position));
        if (mSort != DatabaseOperation.Sort.CUSTOM) holder.getBinding().handle.setVisibility(View.GONE);
        else holder.getBinding().handle.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public void onItemDismiss(int position) {
        ProfileViewModel u = mUsers.get(position);
        BUS.post(new ChangeUserInternalId(u.getRemoteId(), null));
        mFilter.getList().remove(u);
        mUsers.remove(u);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        BUS.post(new ChangeUserInternalId(mUsers.get(fromPosition).getRemoteId(), mUsers.get(toPosition).getRemoteId()));
        Collections.swap(mUsers, fromPosition, toPosition);
        Collections.swap(mFilter.getList(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<ProfileViewModel> getUsers() {
        return mUsers;
    }

    private void setUsers(List<ProfileViewModel> users) {
        if (AppUtils.compareLists(users, mUsers)) return;
        synchronized (this) {
            mUsers = users;
        }
    }

    public void setUsersFromDB(final List<UserEntity> users, DatabaseOperation.Sort sort) {
        synchronized (this) {
            mUsers = new ArrayList<ProfileViewModel>() {{
                for (UserEntity u : users) {
                    add(new ProfileViewModel(u));
                }
            }};
            mSort = sort;
        }
        notifyDataSetChanged();
    }

    public class ChangeUserInternalId {
        final String firstUserRemoteId;
        final String secondUserRemoteId;

        public ChangeUserInternalId(String firstUserRemoteId, String secondUserRemoteId) {
            this.firstUserRemoteId = firstUserRemoteId;
            this.secondUserRemoteId = secondUserRemoteId;
        }

        public String getFirstUserRemoteId() {
            return firstUserRemoteId;
        }

        public String getSecondUserRemoteId() {
            return secondUserRemoteId;
        }
    }

    public void setSort(DatabaseOperation.Sort sort) {
        mSort = sort;
    }

    @Override
    public DatabaseOperation.Sort getSort() {
        return mSort;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: ViewHolder
    public static class UserViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private ItemUserListBinding mBinding;

        @SuppressWarnings("deprecation")
        public UserViewHolder(View itemView, OnItemCLickListener viewClickListener, OnItemCLickListener likesClickListener, OnStartDragListener dragListener) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);

            mBinding.handle.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    dragListener.onStartDrag(this);
                }
                return false;
            });

            mBinding.listMoreInfoBtn.setOnClickListener(v -> {
                if (viewClickListener != null) {
                    viewClickListener.onItemClick(getAdapterPosition());
                }
            });

            mBinding.listLikeBtn.setOnClickListener(v -> {
                if (likesClickListener != null) {
                    likesClickListener.onItemClick(getAdapterPosition());
                }
            });

            mBinding.listUnlikeBtn.setOnClickListener(v -> {
                if (likesClickListener != null) {
                    likesClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        public ItemUserListBinding getBinding() {
            return mBinding;
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
            getBinding().getProfile().setMoving(false);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Filter
    public static class CustomUserListFilter extends Filter {

        private final UsersAdapter mAdapter;
        private final List<ProfileViewModel> mList;

        public List<ProfileViewModel> getList() {
            return mList;
        }

        public CustomUserListFilter(UsersAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
            this.mList = mAdapter.getUsers();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<ProfileViewModel> tempList = new ArrayList<>();

            if (constraint.length() != 0) {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final ProfileViewModel s : mList) {
                    if (s.getFullName().toLowerCase().contains(filterPattern) ||
                            s.getHometask().contains(filterPattern)) {
                        tempList.add(s);
                    }
                }
            } else {
                tempList = mList;
            }
            mAdapter.setUsers(tempList);
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            this.mAdapter.notifyDataSetChanged();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}

