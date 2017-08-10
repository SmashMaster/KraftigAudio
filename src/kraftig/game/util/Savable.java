package kraftig.game.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Savable
{
    public void save(DataOutputStream out) throws IOException;
    public void load(DataInputStream in) throws IOException;
}
