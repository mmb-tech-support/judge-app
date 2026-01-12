package ru.mmb.sportiduinomanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.mmb.sportiduinomanager.R;
import ru.mmb.sportiduinomanager.model.Records;

/**
 * Provides the list of teams punched at a station.
 */
public class TeamListAdapter extends ListAdapter<TeamListAdapter.TeamView, TeamListAdapter.TeamHolder> {
    private final OnSelect mOnSelect;

    /**
     * `ListAdapter.submitList` is asynchronous, so we could not use `.getCurrentList` right after
     * `.submitList`. This list updates immediately in overrided submitList.
     */
    @Getter
    private List<TeamView> mTeamViewList = new ArrayList<>();
    @Getter
    private TeamView mCurrentSelected = null;
    private static final DiffUtil.ItemCallback<TeamView> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TeamView oldItem, @NonNull TeamView newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TeamView oldItem, @NonNull TeamView newItem) {
            return false;
        }
    };

    /**
     * Callback definition.
     */
    @FunctionalInterface
    public interface OnSelect {
        void accept(TeamView team);
    }



    public TeamListAdapter(OnSelect onTeamSelect) {
        super(DIFF_CALLBACK);
        this.mOnSelect = onTeamSelect;
    }

    public int getInvertedPositionOfSelectedOrZero() {
        if (mCurrentSelected == null) return 0;
        return mCurrentSelected.getMPointsPunchPositionInverted();
    }

    @NonNull
    @Override
    public TeamHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.team_list_item, parent, false);
        TeamHolder holder = new TeamHolder(view);
        view.setOnClickListener(v -> selectTeam(holder.getMTeamView()));
        return holder;
    }

    /**
     * finds TeamView in current list with specified invertedPosition.
     *
     * @param invertedPosition - inverted position in Records
     * @return TeamView or null if there are no teamView exists in current list with specified position
     */
    public TeamView findTeamByInvertedPosition(int invertedPosition) {
        for (TeamView teamView : getMTeamViewList()) {
            if (teamView.getMPointsPunchPositionInverted() == invertedPosition) {
                return teamView;
            }
        }
        return null;
    }

    /**
     * Change the selected rows' flags to reflect the new selection and call the callback.
     * If the specified row matches the current selection, do nothing.
     *
     * @param teamView - TeamView of new selection
     */
    public void selectTeam(TeamView teamView) {
        if (mCurrentSelected == teamView) {
            return;
        }

        TeamView oldSelected = mCurrentSelected;
        mCurrentSelected = teamView;

        if (oldSelected != null) {
            int oldPosition = getCurrentList().indexOf(oldSelected);
            this.notifyItemChanged(oldPosition);
        }

        if (teamView != null) {
            int position = getCurrentList().indexOf(teamView);
            this.notifyItemChanged(position);
        }
        mOnSelect.accept(teamView);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamListAdapter.TeamHolder holder, int position) {
        TeamView teamView = this.getItem(position);
        holder.bind(teamView, teamView.equals(mCurrentSelected));
    }

    /**
     * Realization of RecyclerView.ViewHolder for TeamListAdapter.
     */
    public static final class TeamHolder extends RecyclerView.ViewHolder {
        @Getter
        TeamView mTeamView;
        /**
         * Team number and name.
         */
        private final TextView mName;
        /**
         * Current number of members computed from team mask.
         */
        private final TextView mCount;
        /**
         * Time of last punch for the team.
         */
        private final TextView mTime;

        /**
         * Binds data of teamView to component holder.
         *
         * @param teamView - teamView
         * @param selected - flag indicating that the row should be marked as selected
         */
        public void bind(TeamView teamView, boolean selected) {
            this.mTeamView = teamView;
            mName.setText(itemView.getResources().getString(R.string.cp_team_name,
                    teamView.getMTeamNumber(), teamView.getMTeamName()));
            mCount.setText(itemView.getResources().getString(R.string.list_team_count,
                    teamView.getMTeamMembersCount()));
            mTime.setText(itemView.getResources().getString(R.string.list_team_time,
                    Records.printTime(teamView.getMPointTime(), "dd.MM  HH:mm:ss")));
            itemView.setSelected(selected);
        }

        /**
         * Holder for list element containing checkbox with team member name.
         *
         * @param view View of list item
         */
        private TeamHolder(final View view) {
            super(view);
            mName = view.findViewById(R.id.list_team_name);
            mCount = view.findViewById(R.id.list_team_count);
            mTime = view.findViewById(R.id.list_team_time);
        }
    }

    /**
     * Readonly pojo class with all information needed for team row in this RecyclerView.
     */
    @Getter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Builder
    public static class TeamView {
        @EqualsAndHashCode.Include
        final int mTeamNumber;

        final String mTeamName;
        final long mPointTime;
        final int mTeamMembersCount;

        final int mPointsPunchPosition;
        final int mPointsPunchPositionInverted;
    }

    @Override
    public void submitList(@Nullable List<TeamView> list) {
        super.submitList(list);
        this.mTeamViewList = list;
    }
}
