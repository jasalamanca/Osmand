package net.osmand.plus.voice;

import java.util.List;

import alice.tuprolog.Struct;

public interface CommandPlayer {

	String getCurrentVoice();

	CommandBuilder newCommandBuilder();

	void playCommands(CommandBuilder builder);

	void clear();

	List<String> execute(List<Struct> listStruct);
	
	void updateAudioStream(int streamType);

    String getLanguage();
    
    boolean supportsStructuredStreetNames();

	void stop();
}
