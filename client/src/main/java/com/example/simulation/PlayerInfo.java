package com.example.simulation;

import com.example.ui.views.game.Tetrion;

import java.lang.foreign.MemorySegment;

record PlayerInfo(int playerId, Tetrion tetrion, MemorySegment obpfTetrion) {
}
