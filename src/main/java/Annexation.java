import arc.Events;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams.*;
import mindustry.gen.Building;
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

        //load config
        Properties props = new Properties();
        try(InputStream resourceStream = Annexation.class.getResourceAsStream("config.properties")) {
            props.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        winScore = Integer.parseInt(props.getProperty("winScore"));
        updateInterval = Integer.parseInt(props.getProperty("updateInterval"));

        Timer.schedule(() -> {

            Seq<TeamData> teams = Vars.state.teams.present;
            for (TeamData team : teams) {
                int scoreIncrease = 0;
                if (team.team == Team.derelict) continue;
                for (Building core : team.cores) {
                    scoreIncrease += core.block.size;
                }
                lastIncrease.put(team.team, scoreIncrease);
                scores.put(team.team, scores.getOrDefault(team.team, 0) + scoreIncrease);
            }

            Map.Entry<Team, Integer> maxScore = null;
            for (Map.Entry<Team, Integer> score : scores.entrySet()) {
                if (maxScore == null || score.getValue() > maxScore.getValue()) {
                    maxScore = score;
                }
            }

            if (maxScore != null) {
                int bestScore = maxScore.getValue();
                if (bestScore > winScore) {
                    Team winner = maxScore.getKey();
                    Events.fire(new EventType.GameOverEvent(winner));
                }
            }

            scores.entrySet().removeIf(entry -> !entry.getKey().active());
            lastIncrease.entrySet().removeIf(entry -> !entry.getKey().active());

            String progress = "winscore is " + winScore;
            for (Team team : scores.keySet()) {
                progress += "\n[#" + team.color.toString() + "]" + team.name + " : " + scores.getOrDefault(team, 0) + " + " + lastIncrease.getOrDefault(team, 0) + "[]";
            }
            Call.infoPopup(progress, updateInterval, Align.bottom, 0, 0, 0, 0);

        }, 0, updateInterval);

        Events.on(EventType.WorldLoadEvent.class, e -> {
            scores.clear();
            lastIncrease.clear();
        });
    }
}
