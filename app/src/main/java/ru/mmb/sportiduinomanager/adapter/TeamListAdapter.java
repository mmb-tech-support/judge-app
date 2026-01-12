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
    private final OnSelect onSelect;
    @Getter
    List<TeamView> teamViewList = new ArrayList<>();
    @Getter
    private TeamView currentSelected = null;
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

    @FunctionalInterface
    public interface OnSelect {
        void accept(TeamView team);
    }



    public TeamListAdapter(OnSelect onTeamSelect) {
        super(DIFF_CALLBACK);
        this.onSelect = onTeamSelect;
    }

    public int getInvertedPositionOfSelectedOrZero() {
        if (currentSelected == null) return 0;
        return currentSelected.getPointsPunchPositionInverted();
    }

    @NonNull
    @Override
    public TeamHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.team_list_item, parent, false);
        TeamHolder holder = new TeamHolder(view);
        view.setOnClickListener(v -> selectTeam(holder.getTeamView()));
        return holder;
    }

    public TeamView findTeamByInvertedPosition(int invertedPosition) {
        for(TeamView teamView : getTeamViewList()) {
            if (teamView.getPointsPunchPositionInverted() == invertedPosition) return teamView;
        }
        return null;
    }

    public void selectTeam(TeamView teamView) {
        if (currentSelected == teamView)
            return;

        TeamView oldSelected = currentSelected;
        currentSelected = teamView;

        if (oldSelected != null) {
            int oldPosition = getCurrentList().indexOf(oldSelected);
            this.notifyItemChanged(oldPosition);
        }

        if (teamView != null) {
            int position = getCurrentList().indexOf(teamView);
            this.notifyItemChanged(position);
        }
        onSelect.accept(teamView);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamListAdapter.TeamHolder holder, int position) {
        TeamView teamView = this.getItem(position);
        holder.bind(teamView, teamView.equals(currentSelected));
    }

    public static final class TeamHolder extends RecyclerView.ViewHolder {
        @Getter
        TeamView teamView;
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

        public void bind(TeamView teamView, boolean selected) {
            this.teamView = teamView;
            mName.setText(itemView.getResources().getString(R.string.cp_team_name,
                    teamView.getTeamNumber(), teamView.getTeamName()));
            mCount.setText(itemView.getResources().getString(R.string.list_team_count,
                    teamView.getTeamMembersCount()));
            mTime.setText(itemView.getResources().getString(R.string.list_team_time,
                    Records.printTime(teamView.getPointTime(), "dd.MM  HH:mm:ss")));
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

    @Getter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Builder
    public static class TeamView {
        @EqualsAndHashCode.Include
        final int teamNumber;

        final String teamName;
        final long pointTime;
        final int teamMembersCount;

        final int pointsPunchPosition;
        final int pointsPunchPositionInverted;
    }

    @Override
    public void submitList(@Nullable List<TeamView> list) {
        super.submitList(list);
        this.teamViewList = list;
    }
}
