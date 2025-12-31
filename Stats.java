package dev.main;

public class Stats implements Component {
    public int hp;
    public int maxHp;
    public int attack;
    public int defense;
    
    public Stats(int hp, int maxHp, int attack, int defense) {
        this.hp = hp;
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
    }
}
