/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package computer;

import computer.tools.FlacAudioRecorder;
import computer.tools.GoogleSpeechAPIConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Mixer;

/**
 *
 * @author stk
 */
public class VoiceControlDaemon implements Runnable {

    List<CommandRecognitionListener> commandListeners = new ArrayList<>();
    List<String> commands = new ArrayList<>();
    public String name = "VoiceCaptureDaemon";
    private boolean terminated = false;
    private Mixer mixer;
    private List<Mixer> mixers = new ArrayList<>();
    private FlacAudioRecorder recorder = new FlacAudioRecorder();
    private GoogleSpeechAPIConverter converter = new GoogleSpeechAPIConverter();
    private boolean protect;
    private boolean recording;

    public void setup() {
    }

    public void addCommandListener(CommandRecognitionListener l) {
        commandListeners.add(l);
    }

    public void addCommand(String c) {

        while (protect) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        protect = true;
        commands.add(c);
        protect = false;
    }

    @Override
    public void run() {

        cycle();
    }

    @Override
    public boolean terminated() {
        return terminated;
    }

    @Override
    public void terminate() {
        terminated = true;
    }

    @Override
    public void cycle() {
        while (!terminated()) {
            try {
                while(recording) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                recording = true;
                String[] firstCommand = converter.convert(recorder.record(2000));
                recording = false;
                if (firstCommand != null) {
                    while (protect) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    protect = true;
                    for (String command : commands) {
                        if (firstCommand[0].equals(command)) {
                            for (CommandRecognitionListener l : commandListeners) {
                                l.commandRecognized(command);
                            }
                        }
                    }
                    protect = false;
                }
            } catch (IOException ex) {
                Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    public String getName() {
        return name;
    }
    
    public String[] getSentence() {
        while(recording) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        recording = true;
        String[] response = null;
        try {
            response = converter.convert(recorder.record(10000));
        } catch (IOException ex) {
            Logger.getLogger(VoiceControlDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
        recording = false;
        return response;
    }
}
