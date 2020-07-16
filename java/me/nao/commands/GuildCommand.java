package me.nao.commands;

import me.nao.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.io.File;
import java.util.Scanner;

public class GuildCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if ( event.getMessage().getAuthor().isBot() || event.getMessage().isSuppressedEmbeds() )
            return;

        User u = event.getAuthor();
        if ( Bot.G.tempUser != null && u != Bot.G.tempUser ) {
            System.out.println( "blocked" );
            return;
        }

        boolean success = false;

        String args[] = event.getMessage().getContentRaw().split( "\n" );
        if ( args != null && args[0].length() != 0 && args[0].charAt(0) == '>') {
            System.out.println("respond skipped");
            return;
        }
        for ( String arg : args ) {
            System.out.println( arg );
            if ( arg.length() > 4 && arg.substring( 0, 2 ).equals( "||" ) && arg.substring( arg.length() - 2, arg.length() ).equals( "||" ) )
                arg = arg.substring( 2, arg.length() - 2 );

            if ( arg.length() > 4 && ( arg.substring( 0, 4 ).equals( "||ht" ) || arg.substring( 0, 4 ).equals( "http" ) ) ) {
                if ( arg.substring( 0, 4 ).equals( "||ht" ) )
                    arg = arg.substring( 2, arg.length() - 2 );
                System.out.println( "hello" );
                String imageUrl = arg;
                imageUrl = imageUrl;
                setImageUrl( imageUrl, event );
                success = writeContent( imageUrl );
            }
            else if ( isValid( arg ) ) {
                if ( arg.substring( 0, 2 ).equals( "| " ) )
                    arg = arg.substring( 2, arg.length() );

                try{
                    Bot.G.Content newCont = Bot.G.ContentParser( arg );
                    Bot.G.tempUser = event.getAuthor();
                    if ( Bot.G.contents.containsKey( newCont.serie ) ) {
                        System.out.println("Content exists.");
                        event.getChannel().sendMessage( "I've updated " + newCont.serie + " for you :D" ).queue();
                        // return;
                    }

                    Bot.G.contents.put( newCont.serie, newCont );
                    for ( int i = 0 ; i < newCont.tags.size() ; i++ ) {
                        Bot.G.contentTags.add( newCont.serie + newCont.tags.get( i ) );
                    }
                }
                catch( Exception e ) {
                    System.out.println( e );
                    event.getChannel().sendMessage( "I can't read it :(" ).queue();
                }
            }

            // query
            else if ( arg.length() > 1 && arg.charAt( 0 ) == '$' ) {
                query( arg, event );
            }
            else if ( arg.length() > 1 && arg.charAt( 0 ) == '!' ) {
                morning( arg, event );
            }
        }

        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        int counter = 0;
        for ( Message.Attachment atta : attachments ) {
            System.out.println( counter );
            System.out.println( "attachment detected" );
            if ( Bot.G.newContent && atta.isImage() ) {
                String imageUrl = atta.getUrl();
                setImageUrl( imageUrl, event );
                success = writeContent( imageUrl );
            }
            counter++;
        }

        /*
        Use your own emotes
         */
        String me = "";
        String sevenSea = "";
        String seed = "";

        String chu = "";
        String shy = "";
        String broken = "";
        String champ = "";
        String dog = "";

        if ( success )
            addEmote( event, new String[]{ broken, champ, dog } );
    }

    private void listCurrent( GuildMessageReceivedEvent event ) {
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

    private void morning(String arg, GuildMessageReceivedEvent event) {
        arg = arg.substring( 1, arg.length() ).trim();
        if ( event.getChannel().isNSFW() && arg.equals("summarize") ) {
            try {
                listCurrent( event );
                System.out.println( "now summarizing" );
            }
            catch(Exception e) {
                System.out.println( e );
            }
        }
        else if ( arg.equals("export") ) {
            event.getChannel().sendFile(new File("contents.txt"), "content.txt").queue();
        }
    }

    private boolean writeContent(String imageUrl) {
        try {
            System.out.println( "Content appended." );
            Files.write( Paths.get("contents.txt")
                    , ( Bot.G.tempArg + "\n" + imageUrl + "\n" ).getBytes() , StandardOpenOption.APPEND );
            Bot.G.tempArg = "";
            return true;

        } catch (Exception e) {
            // exception handling left as an exercise for the reader
            System.out.println("failed to append new content.");
            return false;
        }
    }

    private void setImageUrl( String imageUrl, GuildMessageReceivedEvent event ) {
        if ( imageUrl.length() > 4 && imageUrl.substring( 0, 2 ).equals( "||" ) && imageUrl.substring( imageUrl.length() - 2, imageUrl.length() ).equals( "||" ) )
            imageUrl = imageUrl.substring( 2, imageUrl.length() - 2 );

        if ( Bot.G.newContent ) {
            if ( Bot.G.contents.containsKey( Bot.G.tempKey ) ) {
                Bot.G.Content tempCont;
                tempCont = Bot.G.contents.get( Bot.G.tempKey );
                tempCont.url = imageUrl;
                Bot.G.contents.put( tempCont.serie, tempCont );
                // printContent( Bot.G.tempKey );

                // String tempStr = getContentString( tempCont );
                // event.getChannel().sendMessage( tempStr ).queue();
                Bot.G.newContent = false;
                Bot.G.tempKey = "";
                Bot.G.tempUser = null;
            }
        }
    }

    private boolean isValid(String arg) {
        int count = 0;
        for ( int i = 0 ; i < arg.length() ; i++ ) {
            if ( arg.charAt( i ) == '|' )
                count++;
        }

        return count >= 4;
    }

    private void printContent( String key ) {
        if ( !Bot.G.contents.containsKey( key ) )
            return;

        Bot.G.Content tempCont = Bot.G.contents.get( key );
        System.out.println( tempCont.serie );
        for ( int i = 0 ; i < tempCont.names.size() ; i++ )
            System.out.println( tempCont.names.get( i ) );
        System.out.println( tempCont.source );
        System.out.println( tempCont.sourceLink );
        System.out.println( tempCont.url );
        for ( int i = 0 ; i < tempCont.tags.size() ; i++ )
            System.out.println( tempCont.tags.get( i ) );
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

    void addEmote( GuildMessageReceivedEvent event, String[] emotes ) {
        for ( int i = 0 ; i < emotes.length ; i++ ) {
            try {
                event.getMessage().addReaction( emotes[i] ).queue();
                System.out.println("emote added");
            }
            catch( Exception e ) {
                System.out.println( "Emote doesn't exist." );
            }
        }
    }

    void query(String arg, GuildMessageReceivedEvent event) {
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
                tempCont.url.replace("|", "");
                tempCont.url = "||" + tempCont.url + "||";
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
}
