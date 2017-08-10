package kraftig.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class PanelSaver
{
    public static final void save(Panel panel, DataOutputStream out) throws IOException
    {
        String name = panel.getClass().getName();
        out.writeUTF(name);
        panel.save(out);
    }
    
    public static final Panel load(DataInputStream in) throws IOException
    {
        try
        {
            String name = in.readUTF();
            Class<? extends Panel> clz = (Class<? extends Panel>)Class.forName(name);
            Constructor<? extends Panel> c = clz.getConstructor();
            Panel out = c.newInstance();
            out.load(in);
            return out;
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
