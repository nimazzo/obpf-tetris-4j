package com.example.simulation;

import com.example.ui.Tetrion;

import java.lang.foreign.MemorySegment;

record PlayerInfo(int playerId, Tetrion tetrion, MemorySegment obpfTetrion) {
}