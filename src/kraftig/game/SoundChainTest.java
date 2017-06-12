package kraftig.game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundChainTest
{
    private static final int SAMPLE_RATE = 48000;
    
    private final TargetDataLine inputLine;
    private final byte[] buffer;
    private final float[] leftBuffer, rightBuffer;
    private final BiquadFilter leftLPF, rightLPF;
    private final float[] leftOutBuffer, rightOutBuffer;
    private final SourceDataLine outputLine;
    
    SoundChainTest() throws Exception
    {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
        
        inputLine = AudioSystem.getTargetDataLine(format);
        inputLine.open();
        inputLine.start();
        
        buffer = new byte[inputLine.getBufferSize()];
        leftBuffer = new float[buffer.length/4];
        rightBuffer = new float[buffer.length/4];
        
        leftLPF = new BiquadFilter(SAMPLE_RATE, 100f, 1.0f);
        rightLPF = new BiquadFilter(SAMPLE_RATE, 100f, 1.0f);
        leftOutBuffer = new float[buffer.length/4];
        rightOutBuffer = new float[buffer.length/4];
        
        outputLine = AudioSystem.getSourceDataLine(format);
        outputLine.open();
        outputLine.start();
    }
    
    void step(float dt)
    {
        //Buffer input byte data.
        int available = inputLine.available();
        inputLine.read(buffer, 0, available);
        
        //Convert to floating point PCM.
        for (int i=0, k=0; i<available;)
        {
            short left = (short)((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8));
            short right = (short)((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8));
            
            leftBuffer[k] = (left + 0.5f)/32767.5f;
            rightBuffer[k++] = (right + 0.5f)/32767.5f;
        }
        
        //Apply low-pass filter.
        int fAvailable = available/4;
        leftLPF.apply(leftBuffer, leftOutBuffer, fAvailable);
        rightLPF.apply(rightBuffer, rightOutBuffer, fAvailable);
        
        //Convert back to byte data.
        for (int i=0, k=0; k<fAvailable;)
        {
            float fLeft = Math.min(Math.max(leftOutBuffer[k], -1.0f), 1.0f);
            float fRight = Math.min(Math.max(rightOutBuffer[k++], -1.0f), 1.0f);
            
            short left = (short)Math.round(fLeft*32767.5f - 0.5f);
            short right = (short)Math.round(fRight*32767.5f - 0.5f);
            
            buffer[i++] = (byte)(left & 0xFF);
            buffer[i++] = (byte)((left >>> 8) & 0xFF);
            buffer[i++] = (byte)(right & 0xFF);
            buffer[i++] = (byte)((right >>> 8) & 0xFF);
        }
        
        //Send to output.
        outputLine.write(buffer, 0, available);
    }
}
