/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back.Objects;

import losersengine.back.CrazyMotors_Back.RaceGame;

/**
 *
 * @author brisin
 */
public class FinishLine extends Prop{
    
    RaceGame Game;

    public FinishLine(float[] pos, RaceGame game) {
        super(pos, new int[]{100,-1860});
        
        Game = game;
        this.type = "finishLine";
    }

    @Override
    public void onCollision(Racer raz) {
        Game.stopTimer(raz);
    }
    
}
