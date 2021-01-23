import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.mod.Plugin;

import java.util.HashMap;
import java.util.Map;

public class Annexation extends Plugin {
    HashMap<Team, Integer> scores = new HashMap<>();
    HashMap<Team, Integer> lastIncrease = new HashMap<>();

    int winScore = 500;
    int updateInterval = 10;

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

            if (maxScore != null) {
                var bestScore = maxScore.getValue();
                if (bestScore > winScore) {
                    var winner = maxScore.getKey();
                    Events.fire(new EventType.GameOverEvent(winner));
                }
            }

            var progress = "winscore is " + winScore;
            for (var team : scores.keySet()) {
                if (team.active()) {
                    progress += "\n[#" + team.color.toString() + "]" + team.name + " : " + scores.getOrDefault(team, 0) + " + " + lastIncrease.getOrDefault(team, 0) + "[]";
                } else {
                    scores.remove(team);
                    lastIncrease.remove(team);
                }
            }
            Call.setHudTextReliable(progress);

        }, 0, updateInterval);
    }
}
