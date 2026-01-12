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
    /**
     * callback.
     */
    private final OnSelect mOnSelect;

    /**
     * `ListAdapter.submitList` is asynchronous, so we could not
     *  use `.getCurrentList` right after `.submitList`.
     *  This list updates immediately in overrided submitList.
     */
    @Getter
    private List<TeamView> mTeamViewList = new ArrayList<>();

    /**
     * current selected or null.
     */
    @Getter
    private TeamView mCurrentSelected;

    /**
     * DiffUtil realization for TeamListAdapter.
     */
    private static final DiffUtil.ItemCallback<TeamView> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull final TeamView oldItem, @NonNull final TeamView newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull final TeamView oldItem, @NonNull final TeamView newItem) {
            return false;
        }
    };

    /**
     * Callback definition.
     */
    @FunctionalInterface
    public interface OnSelect {
        /**
         * callback method.
         *
         * @param team - TeamView of selected team
         */
        void accept(TeamView team);
    }


    /**
     * Constructor.
     *
     * @param onTeamSelect - callback
     */
    public TeamListAdapter(final OnSelect onTeamSelect) {
        super(DIFF_CALLBACK);
        this.mOnSelect = onTeamSelect;
    }

    /**
     * Find in current selected team and returns it's
     * inverted position in Records class.
     * Or zero if no teamView found in list
     *
     * @return inverted position or zero
     */
    public int getInvertedPositionOfSelectedOrZero() {
        if (mCurrentSelected == null) return 0;
        return mCurrentSelected.getMPointsPunchPositionInverted();
    }

    @NonNull
    @Override
    public TeamHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.team_list_item, parent, false);
        final TeamHolder holder = new TeamHolder(view);
        view.setOnClickListener(v -> selectTeam(holder.getMTeamView()));
        return holder;
    }

    /**
     * finds TeamView in current list with specified invertedPosition.
     *
     * @param invertedPosition - inverted position in Records
     * @return TeamView or null if there are no teamView exists in
     *     current list with specified position
     */
    public TeamView findTeamByInvertedPosition(final int invertedPosition) {
        for (final TeamView teamView : getMTeamViewList()) {
            if (teamView.getMPointsPunchPositionInverted() == invertedPosition) {
                return teamView;
            }
        }
        return null;
    }

    /**
     * Change the selected rows' flags to reflect the new
     * selection and call the callback.
     * If the specified row matches the current selection, do nothing.
     *
     * @param teamView - TeamView of new selection
     */
    public void selectTeam(final TeamView teamView) {
        if (mCurrentSelected != null && mCurrentSelected.equals(teamView)) {
            return;
        }

        final TeamView oldSelected = mCurrentSelected;
        mCurrentSelected = teamView;

        if (oldSelected != null) {
            final int oldPosition = getCurrentList().indexOf(oldSelected);
            this.notifyItemChanged(oldPosition);
        }

        if (teamView != null) {
            final int position = getCurrentList().indexOf(teamView);
            this.notifyItemChanged(position);
        }
        mOnSelect.accept(teamView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TeamListAdapter.TeamHolder holder, final int position) {
        final TeamView teamView = this.getItem(position);
        holder.bind(teamView, teamView.equals(mCurrentSelected));
    }

    /**
     * Realization of RecyclerView.ViewHolder for TeamListAdapter.
     */
    public static final class TeamHolder extends RecyclerView.ViewHolder {
        /**
         * current TeamView associated with this holder.
         */
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
        public void bind(final TeamView teamView, final boolean selected) {
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
     * Readonly pojo class with all information needed for team row
     * in this RecyclerView.
     */
    @Getter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Builder
    public static class TeamView {
        /**
         * team number.
         */
        @EqualsAndHashCode.Include
        final int mTeamNumber;

        /**
         * team name.
         */
        final String mTeamName;

        /**
         * point time.
         */
        final long mPointTime;

        /**
         * count of actual members in team.
         */
        final int mTeamMembersCount;

        /**
         * index in Records.
         */
        @SuppressWarnings("PMD.LongVariable")
        final int mPointsPunchPosition;

        /**
         * Records.size() - 1 - mPointsPunchPosition.
         */
        @SuppressWarnings("PMD.LongVariable")
        final int mPointsPunchPositionInverted;
    }

    @Override
    public void submitList(@Nullable final List<TeamView> list) {
        super.submitList(list);
        this.mTeamViewList = list;
    }
}
