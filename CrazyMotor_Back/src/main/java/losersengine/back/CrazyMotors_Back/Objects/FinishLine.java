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

    public FinishLine(float[] pos, int[] col, RaceGame game) {
        super(pos, col);
        
        Game = game;
    }

    @Override
    public void onCollision(Racer raz) {
        Game.stopTimer(raz);
    }
    
}
