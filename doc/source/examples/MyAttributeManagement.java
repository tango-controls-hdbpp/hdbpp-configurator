// Example of using the HDB++ configurator API

import org.Tango.hdb_configurator.configurator.HdbAttribute;
import org.Tango.hdb_configurator.configurator.ManageAttributes;
import fr.esrf.TangoDs.Except;
import fr.esrf.Tango.DevFailed;

public class MyAttributeManagement {
    public static void main (String args[]) {
        try {
            // Create a hdb attribute list
            // These attributes are pushed by the device code
            List<HdbAttribute> hdbAttributes = new ArrayList<HdbAttribute>();
            hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass12", true));
            hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass14", true));
            hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass15", true));
            hdbAttributes.add(new HdbAttribute("sr/v-rga/c1-cv6000/mass16", true));

            // Add send these attributes to an event subscriber
            String archiver = "tango/hdb-es/vacuum";
            ManageAttributes.addAttributes(archiver, hdbAttributes);
        } catch (DevFailed e) {
            Except.print_exception(e);
        }
    }
}
