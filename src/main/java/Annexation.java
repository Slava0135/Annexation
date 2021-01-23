import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.mod.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Annexation extends Plugin {
    HashMap<Team, Integer> scores = new HashMap<>();
    HashMap<Team, Integer> lastIncrease = new HashMap<>();

    int winScore = -1;
    int updateInterval = -1;

    @Override
    public void init() {

        Properties props = new Properties();
        try(InputStream resourceStream = Annexation.class.getResourceAsStream("config.properties")) {
            props.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        winScore = Integer.parseInt(props.getProperty("winScore"));
        updateInterval = Integer.parseInt(props.getProperty("updateInterval"));

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
                    scores.clear();
                    lastIncrease.clear();
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
