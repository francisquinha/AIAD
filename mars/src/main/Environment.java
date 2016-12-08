package main;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author diogo
 */
public class Environment {
 
    public int size;
    public int spotters;
    public int producers;
    public int transporters;
    
    public Environment(int size, int spotters, int producers, int transporters) {
        this.size = size;
        this.spotters = spotters;
        this.producers = producers;
        this.transporters = transporters;
    }
    
}
