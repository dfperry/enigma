package dperry.enigma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import dperry.enigma.parts.Reflector;
import dperry.enigma.parts.Rotor;

public class Application {
	private static Application application;
	private Enigma enigma;
	
	
	public static final String CMD_ENCODE = "encode";
	public static final String CMD_DECODE = "decode";
	
	public static final String CMD_QUIT = "quit";
	
	public static final String CMD_INFO_HELP = "help";
	public static final String CMD_INFO_AVAILABLE_ROTORS = "get all rot";
	public static final String CMD_INFO_AVAILABLE_REFLECTORS = "get all ref";
	public static final String CMD_INFO_STATUS = "status";
	
	public static final String CMD_CFG_RESET = "clear config";
	public static final String CMD_CFG_RELOAD = "reload config";
	
	public static final String CMD_CFG_ADV_ROT_ON = "set advance rotors on";
	public static final String CMD_CFG_ADV_ROT_OFF = "set advance rotors off";
	public static final String CMD_CFG_ADV_REF_ON = "set advance reflector on";
	public static final String CMD_CFG_ADV_REF_OFF = "set advance reflector off";
	
	public static final String CMD_CFG_ADD_ROTOR = "add rotor ";
	public static final String CMD_CFG_SET_REFLECTOR = "set reflector ";
	public static final String CMD_CFG_REMOVE_ROTOR = "remove rotor ";
	public static final String CMD_CFG_REMOVE_REFLECTOR = "remove reflector";
	
	public static final String CMD_CFG_SET_POSITIONS = "set positions ";
	
	public static final String CMD_CFG_ADD_CONNECTION = "add connection ";
	public static final String CMD_CFG_REMOVE_CONNECTION = "remove connection ";
	public static final String CMD_CFG_CLEAR_PLUGBOARD = "clear plugboard";
	
	public static final String PREFIX_INFO = " * ";
	public static final String PREFIX_ERR = " # ";
	public static final String PREFIX_WARN = " ! ";
	
	public static final String USER_PROMPT = " > ";
	public static final String USER_PROMPT_STRESS = " >> ";
	
	
	
	public static void main( String args[] ) {

		application = new Application();
		
		application.start();
	}
	
	public void start() {
		
		enigma = new Enigma();
		
		if( readConfig() ) {
			processInput();
		}
		
		cleanup();
	}
	
	/**
	 * Main application loop. User is prompted for commands and given feedback from those
	 * commands until the user requests application termination
	 */
	private void processInput() {
		
		String currLine;
		
		InputStreamReader input;
		BufferedReader reader;
		
		input = new InputStreamReader( System.in );
		reader = new BufferedReader( input );
		
		
		try {
			
			System.out.println( PREFIX_INFO + "Type 'help' for available commands" );
			
			System.out.print( USER_PROMPT );
			while( !(currLine = reader.readLine().trim()).equals( CMD_QUIT ) ) {
				
				if( currLine.length() > 0 ) {
					
					// encode/decode
					
					if( currLine.equals( CMD_ENCODE ) 
							|| currLine.equals( CMD_DECODE ) ) {
						translateMessage( reader, currLine.equals( CMD_ENCODE ) );
					}
					
					// info
					
					else if( currLine.startsWith( CMD_INFO_HELP ) ) {
						printHelp();
					}
					else if( currLine.startsWith( CMD_INFO_AVAILABLE_ROTORS ) ) {
						getAvailableRotors();
					}
					else if( currLine.startsWith( CMD_INFO_AVAILABLE_REFLECTORS ) ) {
						getAvailableReflectors();
					}
					else if( currLine.startsWith( CMD_INFO_STATUS ) ) {
						getStatus();
					}
					
					// configure
					
					else if( currLine.startsWith( CMD_CFG_RESET ) ) {
						reset( reader );
					}
					else if( currLine.startsWith( CMD_CFG_RELOAD ) ) {
						reload( reader );
					}
					else if( currLine.startsWith( CMD_CFG_SET_POSITIONS ) ) {
						setPositions( currLine.substring( CMD_CFG_SET_POSITIONS.length() ).split( " " ) );
						getStatus();
					}
					else if( currLine.equals( CMD_CFG_ADV_ROT_ON ) ) {
						enigma.setAdvanceRotors( true );
					}
					else if( currLine.equals( CMD_CFG_ADV_ROT_OFF ) ) {
						enigma.setAdvanceRotors( false );
					}
					else if( currLine.equals( CMD_CFG_ADV_REF_ON ) ) {
						enigma.setAdvanceReflector( true );
					}
					else if( currLine.equals( CMD_CFG_ADV_REF_OFF ) ) {
						enigma.setAdvanceReflector( false );
					}
					else if( currLine.startsWith( CMD_CFG_REMOVE_ROTOR ) ) {
						ArrayList<Rotor> rotors = enigma.getCurrentRotors();
						if( rotors.size() > 0 ) {
							try {
								Integer index = new Integer( currLine.substring( CMD_CFG_REMOVE_ROTOR.length() ) );
								rotors.remove( (int)index );
							}
							catch( NumberFormatException e ) {
								System.err.println( PREFIX_ERR + "Invalid rotor selection" );
							}
							catch( IndexOutOfBoundsException e ) {
								System.err.println( PREFIX_ERR + "Invalid rotor selection" );
							}
						}
						else {
							System.out.println( PREFIX_ERR + "No rotors in use" );
						}
					}
					else if( currLine.equals( CMD_CFG_REMOVE_REFLECTOR ) ) {
						enigma.setReflector( null );
					}
					
					else if( currLine.startsWith( CMD_CFG_ADD_ROTOR ) ) {
						addRotor( currLine.substring( CMD_CFG_ADD_ROTOR.length() ) );
					}
					else if( currLine.startsWith( CMD_CFG_SET_REFLECTOR ) ) {
						setReflector( currLine.substring( CMD_CFG_SET_REFLECTOR.length() ) );
					}
					else if( currLine.startsWith( CMD_CFG_ADD_CONNECTION ) ) {
						char[] pair = currLine.substring( CMD_CFG_ADD_CONNECTION.length() ).toUpperCase().toCharArray();
						
						if( pair.length == 2 ) {
							if( enigma.addPlugboardConnection( pair[0], pair[1] ) ) {
								System.out.println( PREFIX_INFO + "Added connection " + pair[0] + "-" + pair[1] );
							}
							else {
								System.err.println( PREFIX_ERR + "Connections exist for one or more of the specified endpoints" );
							}
						}
						else {
							System.err.println( PREFIX_ERR + "Incomplete connection info" );
						}
					}
					else if( currLine.startsWith( CMD_CFG_REMOVE_CONNECTION ) ) {
						char ch = currLine.substring( CMD_CFG_REMOVE_CONNECTION.length() ).toUpperCase().toCharArray()[0];
						
						enigma.removePlugboardConnection( ch );
					}
					else if( currLine.equals( CMD_CFG_CLEAR_PLUGBOARD ) ) {
						enigma.resetPlugboard();
					}
					else {
						System.err.println( PREFIX_ERR + "Unrecognized command: " + currLine );
					}
				}
				System.out.print( USER_PROMPT );
			}
			
			System.out.println( PREFIX_INFO + "Done" );
		}
		catch( IOException e ) {
			System.err.println( PREFIX_ERR + "IOException: " + e.getMessage() );
		}
		finally {
			try {
				if( reader != null ) {
					reader.close();
				}
			}
			catch( IOException e ) {
				System.err.println( PREFIX_ERR + "IOException: " + e.getMessage() );
			}
			try {
				if( input != null ) {
					input.close();
				}
			}
			catch( IOException e ) {
				System.err.println( PREFIX_ERR + "IOException: " + e.getMessage() );
			}
		}
	}
	
	private void setReflector( String name ) {
		ArrayList<Reflector> reflectors = enigma.getAvailableReflectors();
		if( reflectors.size() > 0 ) {
			for( Reflector reflector : reflectors ) {
				if( reflector.getName().equals( name ) ) {
					enigma.setReflector( reflector );
					break;
				}
			}
		}
		else {
			System.out.println( PREFIX_ERR + "No reflectors available to use" );
		}
	}
	
	private void addRotor( String name ) {
		ArrayList<Rotor> rotors = enigma.getAvailableRotors();
		if( rotors.size() > 0 ) {
			for( Rotor rotor : rotors ) {
				if( rotor.getName().equals( name ) ) {
					enigma.addRotor( rotor );
					break;
				}
			}
		}
		else {
			System.out.println( PREFIX_ERR + "No rotors available to use" );
		}
	}
	
	private void getAvailableReflectors() {
		System.out.print( PREFIX_INFO );
		for( String reflector : enigma.availableReflectors() ) {
			System.out.print( reflector + " " );
		}
		System.out.print( "\n" );
	}
	
	private void getAvailableRotors() {
		System.out.print( PREFIX_INFO );
		for( String rotor : enigma.availableRotors() ) {
			System.out.print( rotor + " " );
		}
		System.out.print( "\n" );
	}
	
	private void reset( BufferedReader reader ) throws IOException {
		System.out.println("Clear all imported data?" );
		if( reader.readLine().toLowerCase().startsWith( "y" ) ) {
			System.out.println( PREFIX_WARN + "Clearing data" );
			enigma.clear();
		}
		else {
			System.out.println( PREFIX_INFO + "Canceling" );
		}
	}
	
	private void reload( BufferedReader reader ) throws IOException {
		System.out.println("Reload all imported data?" );
		if( reader.readLine().toLowerCase().startsWith( "y" ) ) {
			System.out.println( PREFIX_WARN + "Reloading data" );
			readConfig();
		}
		else {
			System.out.println( PREFIX_INFO + "Canceling" );
		}
	}
	
	private void printHelp() {
		
		System.out.println( "" );
		System.out.println( "Stored Configurations:" );
		System.out.println( "  clear config " );
		System.out.println( "    clears all stored configuration data (rotors, reflectors)" );
		System.out.println( "  reload config" );
		System.out.println( "    loads stored configuration from file (same as restarting app)" );
		System.out.println( "  get all rotors" );
		System.out.println( "    returns the names of all available rotors" );
		System.out.println( "  get all reflectors" );
		System.out.println( "    returns the names of all available reflectors" );
		System.out.println( "" );
		System.out.println( "Rotor Configuration" );
		System.out.println( "  add rotor [name]" );
		System.out.println( "    adds the specified rotor to the end of the active rotor set" );
		System.out.println( "  remove rotor [index]" );
		System.out.println( "    removes the rotor at the specified index (zero-based) from the active rotor set" );
		System.out.println( "  set advance rotor [on|off]" );
		System.out.println( "    (default - on)" );
		System.out.println( "    turns the advancing of the active rotors on or off." );
		System.out.println( "    turning off essentially switches to a simple substitution cipher" );
		System.out.println( "" );
		System.out.println( "Reflector Configuration" );
		System.out.println( "  set reflector [name]" );
		System.out.println( "    set the active reflector to that specified " );
		System.out.println( "  remove reflector" );
		System.out.println( "    removes the active reflector" );
		System.out.println( "  set advance reflector [on|off]" );
		System.out.println( "    (default - off)" );
		System.out.println( "    turns the advancing of the active reflector on or off." );
		System.out.println( "    turning on adds an extra level of encryption" );
		System.out.println( "" );
		System.out.println( "Rotor/Reflector positioning" );
		System.out.println( "  set positions [rotors] [reflector]" );
		System.out.println( "    sets the rotors and reflectors to the specified positions" );
		System.out.println( "" );
		System.out.println( "    example: if using 4 rotors" );
		System.out.println( "      set positions ABCD E" );
		System.out.println( "" );
		System.out.println( "Plugboard configuration" );
		System.out.println( "  add connection [AB]" );
		System.out.println( "    adds a connection between A and B" );
		System.out.println( "  remove connection [A]" );
		System.out.println( "    removes the connection touching A" );
		System.out.println( "  clear plugboard" );
		System.out.println( "    removes all connections from the plugboard" );
		System.out.println( "" );
		System.out.println( "Machine info" );
		System.out.println( "  status" );
		System.out.println( "    displays the status of the machine, as follows:" );
		System.out.println( "" );
		System.out.println( "           Current Configuration:" );
		System.out.println( "    	    + Rotors" );
		System.out.println( "    	        0      gamma: A" );
		System.out.println( "    	        1       beta: A" );
		System.out.println( "    	        2      delta: A" );
		System.out.println( "    	    - Reflector" );
		System.out.println( "    	                zulu: A" );
		System.out.println( "    	      Plugboard" );
		System.out.println( "    	       C-P L-O T-R" );
		System.out.println( "" );
		System.out.println( "      the +/- indicate advancing status (on/off)" );
		System.out.println( "      the rotor/reflector lines give the following information:" );
		System.out.println( "         [rotor #]   name: position" );
		System.out.println( "" );
		System.out.println( "Message translation" );
		System.out.println( "  encode" );
		System.out.println( "    after entering this command, the user is prompted for the message to be encoded." );
		System.out.println( "    the decoded message is displayed in blocks of 5 characters" );
		System.out.println( "  decode" );
		System.out.println( "    after entering this command, the user is prompted for the message to be decoded." );
		System.out.println( "" );
		System.out.println( "Application" );
		System.out.println( "  help" );
		System.out.println( "    displays this message" );
		System.out.println( "  quit" );
		System.out.println( "    exits the application" );
		System.out.println( "" );
	}
	
	private void setPositions( String[] positions ) {
		String rotorPositions = null;
		String reflector = null;
		
		if( positions.length > 0 ) {
			rotorPositions = positions[0].toUpperCase();
		}
		if( positions.length > 1 ) {
			reflector = positions[1].toUpperCase();
		}
		
		ArrayList<Rotor> rotors = enigma.getCurrentRotors();
		
		if( rotorPositions != null && reflector != null 
				&& rotorPositions.length() == rotors.size() && reflector.length() == 1 ) {
			
			for( int i = 0; i < rotors.size(); i++ ) {
				rotors.get( i ).move( rotorPositions.charAt( i ) );
			}
			enigma.getCurrentReflector().move( reflector.charAt( 0 ) );
		}
		else {
			System.err.println( PREFIX_ERR + "Incorrect positions given" );
		}
	}
	
	private void getStatus() {
		System.out.println( PREFIX_INFO + "Current Configuration:" );
		
		System.out.println( "    " + (enigma.getAdvanceRotors() ? "+" : "-") + " Rotors" );
		
		ArrayList<Rotor> rotors = enigma.getCurrentRotors();
		if( rotors != null && rotors.size() > 0 ) {
			int i = 0;
			for( Rotor rotor : enigma.getCurrentRotors() ) {
				System.out.println( String.format("       %2d %10s: %s", i++,  rotor.getName(), rotor.getCurrent() ) );
			}
		}
		else {
			System.out.println( "              NO ROTORS" );
		}
		
		System.out.println( "    " + (enigma.getAdvanceReflector() ? "+" : "-") + " Reflector" );
		
		Reflector reflector = enigma.getCurrentReflector();
		if( reflector != null ) {
			System.out.println( String.format("          %10s: %s", reflector.getName(), reflector.getCurrent() ) );
		}
		else {
			System.out.println( "              NO REFLECTOR" );
		}
		System.out.println( "      Plugboard" );
		
		int i = 0;
		for( Character[] conn : enigma.getPlugboardConnections() ) {
			System.out.print( (i%4==0 ? "       " : "  " ) + conn[0] + "-" + conn[1] + ((++i)%4==0 ? "\n" : "" ) );
		}
		System.out.print( "\n" );
	}
	
	private void translateMessage( BufferedReader reader, boolean encode ) throws IOException {
		if( enigma.isReady() ) {
			System.out.print( USER_PROMPT_STRESS );
			String cipher = reader.readLine().toUpperCase().replaceAll( "[^A-Z]", "" );
			
			if( cipher.length() > 0 ) {
				
				System.out.println( PREFIX_INFO + "Translating message: " + cipher );
				
				System.out.print( USER_PROMPT_STRESS );
				// convert
				int i = 0;
				for( Character character : cipher.toCharArray() ) {
					try {
						System.out.print( enigma.convert( character ) + (++i % 5 == 0 && encode ? " " : "" ) );
					}
					catch( Exception e ) {
						System.err.println( PREFIX_ERR + "Exception: " + e.getMessage() );
					}
				}
				System.out.println( "\n" );
			}
		}
		else {
			System.err.println( PREFIX_ERR + "Machine is not ready" );
		}
	}
	
	private boolean readConfig() {
		
		boolean valid = false;
		FileInputStream input = null;
		try {
			File config = new File( "enigma.config" );
			input = new FileInputStream( config );
			System.out.println( PREFIX_INFO + "Reading configuration from " + config.getAbsolutePath() );
			
			enigma.importConfiguration( input );
			valid = true;
			
		}
		catch( FileNotFoundException e ) {
			System.err.println( PREFIX_ERR + "Config file not found" );
		}
		finally {
			try {
				if( input != null ) {
					input.close();
				}
			}
			catch( IOException e ) {
				System.err.println( PREFIX_ERR + "IOException: " + e.getMessage() );
			}
		}
		return valid;
	}
	
	private void cleanup() {
		
	}
}
