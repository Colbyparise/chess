package network.http;

import model.GameData;

import java.util.Collection;

public record ListOfGames(Collection<GameData> games) {
}