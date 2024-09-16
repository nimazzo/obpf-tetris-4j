package com.example.simulation;

import com.example.ui.game.Tetrion;

import java.lang.foreign.MemorySegment;

record PlayerInfo(int playerId, Tetrion tetrion, MemorySegment obpfTetrion) {
}
