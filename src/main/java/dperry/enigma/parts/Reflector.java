package dperry.enigma.parts;

import java.util.TreeSet;

public class Reflector {

	private Integer[] pairs;
	String name;
	private int currentPosition;

	public Reflector( String name, Integer[] pairs ) {
		this.pairs = pairs;
		this.name = name;
		currentPosition = 0;
	}

	public static boolean isValid( Integer[] pairs ) {
		boolean valid = true;
		
		if( ( pairs != null ) && ( pairs.length == Rotor.ROTOR_SIZE ) ) {
			
			// check that all elements in series are present
			
			TreeSet<Integer> checker = new TreeSet<Integer>();
			for( Integer i : pairs ) {
				checker.add( i );
			}
			if( checker.size() != Rotor.ROTOR_SIZE || checker.first() != Rotor.ROTOR_MIN || checker.last() != Rotor.ROTOR_MAX ) {
				valid = false;
			}
			
			// check that pairs are valid
			
			for( int i = 0; i < Rotor.ROTOR_SIZE; i++ ) {
				
				if( i != pairs[pairs[i]] ) {
					valid = false;
				}
			}
		}
		else {
			valid = false;
		}
		return valid;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder(); 
		String endl = "\n";
		
		sb.append( "Reflector Info" + endl );
		sb.append( "  Name:               " + name + endl );
		
		sb.append( "  Connections:        " );
		for( int i : pairs ) {
			sb.append( i + " " );
		}
		sb.append( endl );
		
		return sb.toString();
	}

	public void reset( int position ) {
		currentPosition = position;
	}

	public void advance() {
		currentPosition = ( currentPosition + 1 ) % Rotor.ROTOR_SIZE;
	}
	
	public void move( Character position ) {
		position = Character.toUpperCase( position );
		int pos = position-'A';
//		System.out.println( "Moving " + name + " to " + (char)(pos+'A') );
		
		currentPosition = pos;
	}

	public Character getCurrent() {
		return (char) (currentPosition + 'A' );
	}

	public int send( int val ) {
		
		int arrayVal = pairs[ 
				                      ( currentPosition + val < 0 ) 
				                      ? ( currentPosition + val + Rotor.ROTOR_SIZE ) 
				                      : ( currentPosition + val ) % Rotor.ROTOR_SIZE
				                     ];
					
		return ( arrayVal - currentPosition < 0 ) 
				? arrayVal - currentPosition + Rotor.ROTOR_SIZE 
				: arrayVal - currentPosition % Rotor.ROTOR_SIZE;
//		return pairs[( currentPosition + val ) % Rotor.ROTOR_SIZE];
	}

	public String getName() {
		return name;
	}
}
