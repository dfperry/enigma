package dperry.enigma.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Plugboard {

	Map<Character, Character> connections;
	
	public Plugboard() {
		connections = new HashMap<Character, Character>();
		
		reset();
	}
	
	public void reset() {
		connections.clear();
		for( char i = 'A'; i <= 'Z'; i++ ) {
			connections.put( i, i );
		}
	}
	
	public ArrayList<Character[]> getConnections() {
		ArrayList<Character[]> connectors = new ArrayList<Character[]>();
		Map<Character,Character> temp = new HashMap<Character,Character>();
		for( char ch : connections.keySet() ) {
			Character k = connections.get( ch );
			if( !k.equals( ch ) ) {
				if( !temp.containsKey( k ) && ! temp.containsValue( k ) ) {
					connectors.add( new Character[]{ch,k} );
					temp.put(k,ch);
				}
			}
		}
		return connectors;
	}
	
	public char getEndpoint( char a ) {
		return connections.get( a );
	}
	
	public void removeConnector( char a ) {
		char old = connections.get( a );
		connections.put( a, a );
		connections.put( old, old );
	}
	
	public boolean addConnection( char char1, char char2 ) {
		
		boolean added = false;
		
		if( char1 != char2 ) {
			
			if( connections.get( char1 ).equals( char1 ) && connections.get( char2 ).equals( char2 ) ) {
				connections.put( char1, char2 );
				connections.put( char2, char1 );
				added = true;
			}
		}
		
		return added;
	}
}
