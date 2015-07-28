package edu.isistan.rolegame.server.resources;

import java.util.Vector;

import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.Round;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.UserMessage;

public interface GameDAO {
	
	public Game createGame(Integer nPlayers, GamePlayer creator);
	
	public boolean deleteGame(Integer idGame);
	
	public Game findGame(Integer idGame);
	
	public Round addRound(Game game, String role);
	
	public boolean addPlayer(GamePlayer player, Game game, String role);
	
	public boolean addUserMessage(UserMessage message, Game game, Round round);
	
	public boolean addVote(GamePlayer fromPlayer, GamePlayer toPlayer, Game game, Round round, Boolean auto);
	
	public boolean setRoundResult(Round round, GamePlayer player);
	
	public boolean setPlayerEliminated(Game game, GamePlayer player, String eliminatedBy);
	
	public boolean setFinal(Game game, String result);
	
	public boolean addArgument(ArgumentMessage message, Game game, Round round);

	public Vector<ArgumentMessage> loadArguments(String player, Game game);
}
