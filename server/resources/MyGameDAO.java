package edu.isistan.rolegame.server.resources;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.Participation;
import edu.isistan.rolegame.shared.ParticipationId;
import edu.isistan.rolegame.shared.Round;
import edu.isistan.rolegame.shared.Vote;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.SimpleArgumentMessage;
import edu.isistan.rolegame.shared.comm.UserMessage;

public class MyGameDAO implements GameDAO {

	private SessionFactory sessionFactory;

	public MyGameDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public MyGameDAO() {
		// Inicializaci�n
	}

	@Override
	public Game createGame(Integer nPlayers, GamePlayer creator) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			Game game = new Game();
			game.setnPlayers(nPlayers);
			game.setCreator(creator);
			game.setDatecreation(new Date());

			session.save(game);

			tx.commit();
			session.close();

			return game;
		} catch (HibernateException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean deleteGame(Integer idGame) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Game findGame(Integer idGame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Round addRound(Game game, String role) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			Round round = new Round(game, role);
			round.setDateBeginning(new Date());
			session.save(round);

			tx.commit();
			session.close();
			return round;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean addPlayer(GamePlayer player, Game game, String role) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			// User user = (User) session.load(User.class, userId);
			// Event theEvent = (Event) session.load(Event.class, eventId);
			// game.addPlayer(player);
			// theEvent.addParticipant(user);

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean addUserMessage(UserMessage message, Game game, Round round) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			// message ya tiene seteado sender, text y type
			// round.addUserMessage(message);
			// Setea en el message el game, round y date
			message.setGame(game);
			message.setRound(round);
			message.setDate(new Date());

			session.save(message);

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean addVote(GamePlayer fromPlayer, GamePlayer toPlayer,
			Game game, Round round, Boolean auto) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			Vote vote = new Vote(round, game, fromPlayer, toPlayer, auto);
			// round.addVote(vote);
			vote.setDate(new Date());
			session.save(vote);

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean setRoundResult(Round round, GamePlayer player) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			// User user = (User) session.load(User.class, userId);
			// user.getEmails().add(email);
			round.setResult(player);
			// Setea el Date de finalizaci�n de la ronda
			round.setDateFinish(new Date());
			session.update(round);

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean setPlayerEliminated(Game game, GamePlayer player,
			String eliminatedBy) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			ParticipationId participationId = new ParticipationId(
					player.getName(), game.getIdGame());
			Participation participation = (Participation) session.get(
					Participation.class, participationId);
			participation.setDateElimination(new Date()); // date actual
			participation.setEliminatedBy(eliminatedBy);

			session.update(participation); // hace falta??

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean setFinal(Game game, String result) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			game.setFinalresult(result);
			game.setDatefinish(new Date());

			session.update(game); // hace falta??

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean addArgument(ArgumentMessage message, Game game, Round round) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			// convierto a simple argument que es guardado en la base
			SimpleArgumentMessage argument = new SimpleArgumentMessage(
					message.toString(), message.getPlayer());
			// round.addUserMessage(message);
			// Setea en el message el game, round y dato
			argument.setGame(game);
			argument.setRound(round);
			argument.setDate(new Date());
			session.save(argument);

			tx.commit();
			session.close();
			return true;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Vector<ArgumentMessage> loadArguments(String player, Game game) {
		try {
			Session session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();

			Vector<ArgumentMessage> result = new Vector<ArgumentMessage>();
			
			Query query = session.createQuery("FROM UserMessage AS A "
											+"LEFT JOIN FETCH A.game "
											+"LEFT JOIN FETCH A.sender "
											+"WHERE A.game = :id");
			query.setParameter("id",game);
			List arguments = query.list();
			
			for (Iterator it = arguments.iterator(); it.hasNext();) {
				/*ArgumentMessage arg = (ArgumentMessage) it.next();
				if (arg.getPlayer().getName()==player)
					result.add(arg);*/
				
				//TESTING
				UserMessage um = (UserMessage) it.next();
				Hibernate.initialize(um);
				if (um.getSender().getName().equals(player)){
					result.add(new SimpleArgumentMessage(um.getText(),um.getSender()));
				}
				//FIN TESTING
			}

			tx.commit();
			session.close();
			return result;
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}
}