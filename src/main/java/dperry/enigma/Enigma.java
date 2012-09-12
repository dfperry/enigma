package dperry.enigma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import dperry.enigma.parts.Plugboard;
import dperry.enigma.parts.Reflector;
import dperry.enigma.parts.Rotor;

public class Enigma {

	ArrayList<Rotor> availableRotors;
	ArrayList<Rotor> currentRotors;
	
	ArrayList<Reflector> availableReflectors;
	Reflector currentReflector;
	boolean advanceRotors;
	boolean advanceReflector;
	
	Plugboard plugboard;
	
	public Enigma() {
		reset();
	}
	
	public void clear() {
		reset();
	}
	
	private void reset() {
		advanceRotors = true;
		advanceReflector = false;
		availableRotors = new ArrayList<Rotor>();
		currentRotors = new ArrayList<Rotor>();
		currentReflector = null;
		availableReflectors = new ArrayList<Reflector>();
		plugboard = new Plugboard();
	}
	
	public boolean isReady() {
//		System.out.println( "Rotor count: " + currentRotors.size() + " reflector " + currentReflector != null ? "set" : "not set" );
		return currentRotors.size() > 0 && currentReflector != null;
	}
	
	public void importConfiguration( InputStream input ) {
		
		reset();
		
		BufferedReader reader = null;
		InputStreamReader stream = null;
		
		try {
			stream = new InputStreamReader( input );
			reader = new BufferedReader( stream );
			
			String currentLine = "";
			
			while ( (currentLine = reader.readLine() ) != null ) {
				if( currentLine.startsWith( "rotor " ) ) {
					
					// parse connections, create new rotor object
					String trimmed = currentLine.substring( "rotor ".length() );
					trimmed = trimmed.trim().replaceAll( "\\s+", " " );
					
					String[] kvps = trimmed.split( " " );
					
					Integer[] turnover = null;
					Integer[] rotorArray = null;
					String name = null;
					
					for( String kvp : kvps ) {
						String[] p = kvp.split( "=" );
						
						if( p[0].equalsIgnoreCase( "name" ) ) {
							name = p[1];
						}
						if( p[0].equalsIgnoreCase( "tl" ) ) {
							String[] stringArray = p[1].split( "," );
							
							turnover = new Integer[stringArray.length];
							for( int i = 0; i < stringArray.length; i++ ) {
								turnover[i] = Integer.parseInt( stringArray[i] );
							}
							
						}
						if( p[0].equalsIgnoreCase( "rc" ) ) {
							String[] stringArray = p[1].split( "," );
							if( stringArray.length == Rotor.ROTOR_SIZE ) {
								rotorArray = new Integer[Rotor.ROTOR_SIZE];
								for( int i = 0; i < stringArray.length; i++ ) {
									rotorArray[i] = Integer.parseInt( stringArray[i] );
								}
							}
						}
					}
					
					if( turnover.length > 0 && Rotor.isValid( rotorArray ) ) {
						availableRotors.add( new Rotor( name, rotorArray, turnover ) );
					}
					else {
						System.err.println( "Invalid rotor configuration: " + currentLine );
					}
				}
				if( currentLine.startsWith( "reflector " ) ) {
					// parse connections, create new rotor object
					String trimmed = currentLine.substring( "reflector ".length() );
					trimmed = trimmed.trim().replaceAll( "\\s+", " " );
					
					String[] kvps = trimmed.split( " " );
					
					Integer[] refArray = null;
					String name = null;
					
					for( String kvp : kvps ) {
						String[] p = kvp.split( "=" );
						
						if( p[0].equalsIgnoreCase( "name" ) ) {
							name = p[1];
						}
						if( p[0].equalsIgnoreCase( "rc" ) ) {
							String[] stringArray = p[1].split( "," );
							if( stringArray.length == Rotor.ROTOR_SIZE ) {
								refArray = new Integer[Rotor.ROTOR_SIZE];
								for( int i = 0; i < stringArray.length; i++ ) {
									refArray[i] = Integer.parseInt( stringArray[i] );
								}
							}
						}
					}
					
					if( Reflector.isValid( refArray ) ) {
						availableReflectors.add( new Reflector( name, refArray ) );
					}
					else {
						System.err.println( " # Invalid reflector configuration: " + currentLine );
					}
				}
			}
		}
		catch( IOException e ) {
			System.err.println( " # IOException: " + e.getMessage() );
		}
		finally {
			if( stream != null ) {
				try {
					stream.close();
				}
				catch( IOException e ) {
					System.err.println( " # IOException: " + e.getMessage() );
				}
			}
			if( reader != null ) {
				try {
					reader.close();
				}
				catch( IOException e ) {
					System.err.println( " # IOException: " + e.getMessage() );
				}
			}
		}
		
		System.out.println( " * Imported " + availableRotors.size() + " rotor configurations:" );
		for( Rotor rotor : availableRotors ) {
			System.out.println( rotor.toString() );
		}
		
		System.out.println( " * Imported " + availableReflectors.size() + " reflector configurations:" );
		for( Reflector reflector : availableReflectors ) {
			System.out.println( reflector.toString() );
		}
	}
	
	public void addRotor( Rotor rotor ) {
		currentRotors.add( new Rotor( rotor ) );
	}
	
	public void setReflector( Reflector reflector ) {
		currentReflector = reflector;
	}
	
	public String[] availableRotors() {
		String[] names = new String[availableRotors.size()];
		int i = 0;
		for( Rotor rotor : availableRotors ) {
			names[i++] = rotor.getName();
		}
		
		return names;
	}
	
	public String[] availableReflectors() {
		String[] names = new String[availableReflectors.size()];
		int i = 0;
		for( Reflector reflector : availableReflectors ) {
			names[i++] = reflector.getName();
		}
		
		return names;
	}
	
	public ArrayList<Character[]> getPlugboardConnections() {
		return plugboard.getConnections();
	}
	
	public char convert( char letter ) throws Exception {
		if( isReady() ) {
			// convert
			
			// check the plugboard
			letter = plugboard.getEndpoint( letter );
			
			int result = (char)(letter-'A');
			
			// send character down the rotors
			for( int i = 0; i < currentRotors.size(); i++ ) {
				result = currentRotors.get( i ).send( result );
			}
			
			// hit the reflector
			result = currentReflector.send( result );
			
			// send the character back down the rotors
			for( int i = currentRotors.size(); i > 0; i-- ) {
				result = currentRotors.get( i-1 ).getReverse( result );
			}
			
			if( advanceRotors ) {
				Character last = currentRotors.get(currentRotors.size()-1).getCurrent();
				// advance the starting rotor, chain the rest
				for( int i = 0; i < currentRotors.size(); i++ ) {
					if( !currentRotors.get( i ).advance() )
						break;
				}
				
				if( advanceReflector ) {
					if( !last.equals( currentRotors.get(currentRotors.size()-1).getCurrent() ) ) {
						currentReflector.advance();
					}
				}
			}
			
			return plugboard.getEndpoint( (char)(result+'A') );
		}
		throw new Exception( "Machine is not ready" );		
	}
	
	public void resetPlugboard() {
		plugboard.reset();
	}
	
	public void removePlugboardConnection( char ch ) {
		plugboard.removeConnector( ch );
	}
	
	public boolean addPlugboardConnection( char char1, char char2 ) {
		return plugboard.addConnection( char1, char2 );
	}

	public ArrayList<Rotor> getAvailableRotors() {
		return availableRotors;
	}

	public ArrayList<Reflector> getAvailableReflectors() {
		return availableReflectors;
	}
	
	public ArrayList<Rotor> getCurrentRotors() {
		return currentRotors;
	}

	public Reflector getCurrentReflector() {
		return currentReflector;
	}

	public Plugboard getPlugboard() {
		return plugboard;
	}
	
	public void setAdvanceRotors( boolean advanceRotors ) {
		this.advanceRotors = advanceRotors;
	}

	public boolean getAdvanceRotors() {
		return advanceRotors;
	}

	public void setAdvanceReflector( boolean advanceReflector ) {
		this.advanceReflector = advanceReflector;
	}

	public boolean getAdvanceReflector() {
		return advanceReflector;
	}
}
