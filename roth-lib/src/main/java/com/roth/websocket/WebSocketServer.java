package com.roth.websocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.roth.base.log.Log;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

public class WebSocketServer {
	// List of active sessions.
	private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
	
	/**
	 * Add a newly opened session, and create the game for the session. 
	 * @param gameId
	 * @param gameType
	 * @param session
	 */
	private static void addSession(Session session, Map<String, Object> props) {
		sessions.add(session);
		String username = session.getUserPrincipal().getName();
		/*
		String gameId = (String)props.get(GAME_ID);
	    GameType gameType = GameType.valueOf((String)props.get(GAME_TYPE));
	    boolean starting = START.equals(gameId);
		if (starting) {
			Game game = Game.startGame(gameType);
			game.join(username);
			gameId = game.getGameId();
		}
		gameFinder.put(session, gameId);
		WsGame game = games.computeIfAbsent(gameId, s -> new WsGame(Game.findGame(s), s));
		game.addSession(gameId, session);
		if (!starting)
			broadcastMessage(gameId, new WsMessage(Action.JOIN, gameType, gameId, username));
		else {
			WsMessage message = new WsMessage(Action.START, gameType, gameId, username);
			sendMessage(message, session);
		}
		*/
	}

	// Remove a closed session
	private static void removeSession(Session session) {
		/*
		String gameId = gameFinder.get(session);
		gameFinder.remove(session);
		WsGame game = games.get(gameId);
		if (game != null) {
			game.removeSession(session);
			if (game.getSessions().isEmpty())
				games.remove(gameId);
		}
		*/
		sessions.remove(session);
	}
	/*
	// Send a message to the user of the session.
	private static void sendMessage(WsMessage message, Session session) {
		String username = session.getUserPrincipal().getName();
		try {
			session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException e) {
			Log.logException(e, username);
		}
	}
	
	// Broadcast a message to all users associated with the game, except the user of the session.
	public static void broadcastMessage(String gameId, WsMessage message) {
		WsGame game = games.get(gameId);
		for (WsSession player : game.getSessions())
			if (!player.getUsername().equals(message.getUsername())) {
				sendMessage(message, player.getSession());
			}
    }
	*/
	private String getUsername(Session session) {
		if (session.getUserPrincipal() != null)
			return session.getUserPrincipal().getName();
		return null;
	}
	
	@OnOpen
	public void onOpen(EndpointConfig endpointConfig, Session session) {
		String username = getUsername(session);
	    Log.logInfo("Web Socket connection opened.", username);
		addSession(session, endpointConfig.getUserProperties());
	}
	
	@OnClose
	public void onClose(Session session) {
		Log.logInfo("Web Socket connection closed.", session.getUserPrincipal().getName());
		removeSession(session);
	    
	}
	
	/*
	@OnMessage
	public WsMessage onMessage(WsMessage message, Session session) {
		String gameId = gameFinder.get(session);
		WsGame game = games.get(gameId);
		WsMessage response = game.handleAction(message, session);
		
		if (message.getAction() == Action.EJECT) {
			WsSession removeSession= game.getSession(message.getMessage());
			if (removeSession != null) {
				sendMessage(response, removeSession.getSession());
				game.removeSession(removeSession.getSession());
			}
		}
	
		if (response != null)
			broadcastMessage(gameId, response);
	    return message;
	}
	*/
	@OnError
	public void onError(Throwable e) {
		Log.log("ERROR", e.getMessage(), "com.jp.cards.socket.WsServer.onError", null, true, e);
	}
}
