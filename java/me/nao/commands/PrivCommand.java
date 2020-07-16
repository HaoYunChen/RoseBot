package me.nao.commands;

import me.nao.Bot;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PrivCommand extends ListenerAdapter {
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        super.onPrivateMessageReceived(event);
        String arg = event.getMessage().getContentRaw();
        if ( event.getAuthor().isBot() )
            return;

        System.out.println( arg );

        if ( arg.length() > 1 && arg.charAt( 0 ) == '$' ) {
            query( arg, event );
        }
        else if ( arg.length() > 1 && arg.charAt( 0 ) == '!' ) {
            morning( arg, event );
        }
    }

    void query(String arg, PrivateMessageReceivedEvent event) {
        // $ tag1 tag2...
        String[] targetTags = arg.substring( 1, arg.length() ).trim().split( "\\s+" );
        List<Bot.G.Content> results = new ArrayList<Bot.G.Content>();

        for ( String key : Bot.G.contents.keySet() ) {
            boolean matchAll = true;
            for ( int i = 0 ; i < targetTags.length ; i++ ) {
            	String alterTarget = "";
            	for ( String k : Bot.G.contents.keySet() ) {
            		for ( String t : Bot.G.contents.get(k).tags ) {
            			if ( t.toLowerCase().contains( targetTags[i].toLowerCase() ) )
            				alterTarget = t;
            		}
            	}
            	
                if ( !Bot.G.contentTags.contains( key + alterTarget ) ) { // can't find the set in the middle table
                    matchAll = false;
                }
            }

            if ( matchAll ) {
                Bot.G.Content tempCont = Bot.G.contents.get( key );
                if ( tempCont.url.length() > 2 && tempCont.url.substring( 0, 2 ).equals( "||" ) )
                    tempCont.url = tempCont.url.substring( 2, tempCont.url.length() - 2 );
                results.add( tempCont );
            }
        }

        for ( int i = 0 ; i < results.size() ; i++ ) {
            event.getChannel().sendMessage( getContentString( results.get( i ) ) ).queue();
            // event.getChannel().sendMessage( results.get( i ).url ).queue();
        }

        if ( results.isEmpty() )
            event.getChannel().sendMessage( "I can't find anything :(" ).queue();
    }
    
    private void listCurrent( PrivateMessageReceivedEvent event ) {
    	event.getChannel().sendMessage( "total pics: " + Bot.G.contents.size() ).queue();

    	Map<String, Integer> hashT = new HashMap<>();

    	for ( Entry<String, Bot.G.Content> entry : Bot.G.contents.entrySet() ) {
    		for ( String s : entry.getValue().tags ) {
    			hashT.put( s, hashT.getOrDefault( s, 0 ) + 1 );
    		}
    	}

    	String tagsCont = "";
    	for ( String s : hashT.keySet() ) {
    		tagsCont += ( s + ": " );
    		tagsCont += ( hashT.get( s ) + " , " );
    		if ( tagsCont.length() > 1500 ) {
    			event.getChannel().sendMessage( tagsCont ).queue();
    			tagsCont = "";
    		}
    	}
    }

    private void morning(String arg, PrivateMessageReceivedEvent event) {
        arg = arg.substring( 1, arg.length() ).trim();
        if ( arg.equals("summarize") ) {
        	try {
        		listCurrent( event );
            	System.out.println( "summarizing" );
        	}
        	catch(Exception e) {
        		System.out.println( e );
        	}	
        }
        else if ( arg.equals("export") ) {
        	event.getChannel().sendFile(new File("contents.txt"), "content.txt").queue();
        }
    }

    String getContentString( Bot.G.Content tempCont ) {
        String tempStr = "";
        tempStr += "Theme: ";
        tempStr += tempCont.serie;
        tempStr += "\t| Characters: ";
        for ( int i = 0 ; i < tempCont.names.size() ; i++ ) {
            tempStr += tempCont.names.get( i );
            if ( i + 1 != tempCont.names.size() )
                tempStr += ", ";
        }
        tempStr += "\t| Artist: ";
        tempStr += tempCont.source;
        tempStr += "\t| Source: ";
        tempStr += tempCont.sourceLink;
        tempStr += "\t| Pic url: ";
        tempStr += tempCont.url;
        tempStr += "\t| Tags: ";
        for ( int i = 0 ; i < tempCont.tags.size() ; i++ ) {
            tempStr += tempCont.tags.get( i );
            if ( i + 1 != tempCont.tags.size() )
                tempStr += ", ";
        }

        return tempStr;
    }
}
