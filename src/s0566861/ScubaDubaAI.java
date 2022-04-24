package s0566861;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

import java.awt.*;
import java.awt.geom.Path2D;

public class ScubaDubaAI extends AI {


    public ScubaDubaAI(Info info) {
        super(info);

        enlistForTournament(566861);
    }

    @Override
    public String getName() {
        return "ScubaDuba";
    }

    @Override
    public Color getPrimaryColor() {
        return Color.BLACK;
    }

    @Override
    public Color getSecondaryColor() {
        return Color.pink;
    }

    @Override
    public PlayerAction update() {

        Point[] pearls = info.getScene().getPearl();
        Path2D[] obstacles = info.getScene().getObstacles();

        return new DivingAction(1, -1.5f);
    }
}
