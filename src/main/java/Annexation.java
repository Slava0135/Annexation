import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.mod.Plugin;

import java.util.HashMap;
import java.util.Map;

public class Annexation extends Plugin {
    HashMap<Team, Integer> scores = new HashMap<>();
    HashMap<Team, Integer> lastIncrease = new HashMap<>();

    int winScore = 1000;

    @Override
    public void init() {

        Timer.schedule(() -> {

            var teams = Vars.state.teams.present;
            for (var team : teams) {
                var scoreIncrease = 0;
                if (team.team == Team.derelict) continue;
                for (var core : team.cores) {
                    scoreIncrease += core.block.size;
                }
                lastIncrease.put(team.team, scoreIncrease);
                scores.put(team.team, scores.getOrDefault(team.team, 0) + scoreIncrease);
            }

            Map.Entry<Team, Integer> maxScore = null;
            for (var score : scores.entrySet()) {
                if (maxScore == null || score.getValue() > maxScore.getValue()) {
                    maxScore = score;
                }
            }
            if (maxScore != null && maxScore.getValue() > winScore) Call.gameOver(maxScore.getKey());

        }, 0, 10f);

        Timer.schedule(() -> {
            var progress = new StringBuilder();
            for (var team : scores.keySet()) {
                if (team.active()) {
                    progress.append(team.name).append(":").append(scores.getOrDefault(team, 0)).append("+").append(lastIncrease.getOrDefault(team, 0)).append("\n");
                } else {
                    scores.remove(team);
                    lastIncrease.remove(team);
                }
            }
            Call.announce(progress.toString());
        }, 0, 1f);
    }
}
