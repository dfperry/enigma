package dperry.enigma.parts;

import java.util.Arrays;
import java.util.TreeSet;

public class Rotor {

	public static final int ROTOR_SIZE = 26;
	public static final int ROTOR_MIN = 0;
	public static final int ROTOR_MAX = ROTOR_SIZE - 1;

	private int currentPosition;
	private String name;

	Integer[] connections;
	Integer[] reverse;
	Integer[] turnover;

	public Rotor( Rotor rotor ) {
		currentPosition = 0;
		this.name = rotor.name;
		this.turnover = rotor.turnover.clone();
		this.connections = rotor.connections.clone();
		
		generateReverseConnections();
	}
	
	public Rotor( String name, Integer[] connections, Integer[] turnover ) {

		currentPosition = 0;
		this.name = name;
		this.turnover = turnover;
		this.connections = connections;

		generateReverseConnections();
	}

	private void generateReverseConnections() {
		reverse = new Integer[ROTOR_SIZE];
		for( int i = 0; i < ROTOR_SIZE; i++ ) {
			reverse[connections[i]] = i;
		}
	}

	public static boolean isValid( Integer[] connections ) {
		boolean valid = true;

		if( ( connections != null ) && ( connections.length == ROTOR_SIZE ) ) {
			TreeSet<Integer> checker = new TreeSet<Integer>();
			for( Integer i : connections ) {
				checker.add( i );
			}
			if( checker.size() != ROTOR_SIZE || checker.first() != ROTOR_MIN || checker.last() != ROTOR_MAX ) {
				valid = false;
			}
		}
		else {
			valid = false;
		}
		return valid;
	}

	public void reset( int position ) {
		currentPosition = position;
	}

	public void move( Character position ) {
		position = Character.toUpperCase( position );
		int pos = position - 'A';
//		System.out.println( "Moving " + name + " to " + (char) ( pos + 'A' ) );

		currentPosition = pos;
	}

	public boolean advance() {
		currentPosition = ( currentPosition + 1 ) % ROTOR_SIZE;
		return Arrays.asList( turnover ).contains( currentPosition );
	}

	public Character getCurrent() {
		return (char) ( currentPosition + 'A' );
	}

	public int send( int val ) {
		// return (connections[val] + currentPosition ) % ROTOR_SIZE;
		// return connections[ ( currentPosition + val ) % ROTOR_SIZE ];
		int arrayVal = connections[ 
		                      ( currentPosition + val < 0 ) 
		                      ? ( currentPosition + val + ROTOR_SIZE ) 
		                      : ( currentPosition + val ) % ROTOR_SIZE
		                     ];
			
		return ( arrayVal - currentPosition < 0 ) ? arrayVal - currentPosition + ROTOR_SIZE : arrayVal - currentPosition % ROTOR_SIZE;
			
			
			
			
//		return ( connections[ 
//		                      ( currentPosition + val < 0 ) 
//		                      ? ( currentPosition + val + ROTOR_SIZE ) 
//		                      : ( currentPosition + val ) % ROTOR_SIZE
//		                     ] - currentPosition ) % ROTOR_SIZE;
	}

	public int getReverse( int val ) {
		// return (reverse[val] + currentPosition ) % ROTOR_SIZE;
		// return reverse[( currentPosition + val ) % ROTOR_SIZE];
		
		int arrayVal = reverse[ 
				                      ( currentPosition + val < 0 ) 
				                      ? ( currentPosition + val + ROTOR_SIZE ) 
				                      : ( currentPosition + val ) % ROTOR_SIZE
				                     ];
					
		return ( arrayVal - currentPosition < 0 ) ? arrayVal - currentPosition + ROTOR_SIZE : arrayVal - currentPosition % ROTOR_SIZE;
				
//		return ( reverse[
//		                 	( currentPosition + val < 0 ) 
//		                 	? ( currentPosition + val + ROTOR_SIZE ) 
//		                 	: ( currentPosition + val ) % ROTOR_SIZE
//		                 ] - currentPosition ) % ROTOR_SIZE;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		String endl = "\n";

		sb.append( "Rotor Info" + endl );
		sb.append( "  Name:               " + name + endl );
		sb.append( "  Turnover Positions: " );
		for( int i : turnover ) {
			sb.append( i + " " );
		}
		sb.append( endl );

		sb.append( "  Connections:        " );
		for( int i : connections ) {
			sb.append( i + " " );
		}
		sb.append( endl );

		sb.append( "  Reverse values:     " );
		for( int i : reverse ) {
			sb.append( i + " " );
		}
		sb.append( endl );

		return sb.toString();
	}

	public String getName() {
		return name;
	}
}
