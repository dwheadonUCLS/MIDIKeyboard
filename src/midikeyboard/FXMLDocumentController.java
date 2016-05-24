/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midikeyboard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 *
 * @author dwheadon
 */
public class FXMLDocumentController implements Initializable, UsbPipeListener {
    
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        // Create the libusb context
        final Context context = new Context();

        // Initialize the libusb context
        int result = LibUsb.init(context);
        // Read the USB device list
        DeviceList list = new DeviceList();
        result = LibUsb.getDeviceList(context, list);
        
        for (Device device: list)
            {
                int address = LibUsb.getDeviceAddress(device);
                int busNumber = LibUsb.getBusNumber(device);
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result < 0)
                {
                    throw new LibUsbException(
                        "Unable to read device descriptor", result);
                }
                System.out.format(
                    "Bus %03d, Device %03d: Vendor %04x, Product %04x%n",
                    busNumber, address, descriptor.idVendor(),
                    descriptor.idProduct());
            }
        
    }
    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UsbHub rootHub = null;
        try {
            rootHub = UsbHostManager.getUsbServices().getRootUsbHub();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        UsbDevice midiKeyboard = findDevice(rootHub, (short) 0x0763, (short) 0x1014);
        UsbConfiguration configuration = midiKeyboard.getActiveUsbConfiguration();
        //UsbConfiguration configuration = (UsbConfiguration) (midiKeyboard.getUsbConfigurations().get(0));
        UsbInterface iface = configuration.getUsbInterface((byte) 0);
//        int blah = iface.
//        try {
//            iface.claim();
//        } catch (Exception ex) {
//            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x03);
        UsbPipe pipe = endpoint.getUsbPipe();
        try {
            pipe.open();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        pipe.addUsbPipeListener(this);
//        handleButtonAction(null);
    }
    
    @Override
    public void errorEventOccurred(UsbPipeErrorEvent event)
    {
        //UsbException error = event.getUsbException();
        //... Handle error ...
    }
    
    @Override
    public void dataEventOccurred(UsbPipeDataEvent event)
    {
        byte[] data = event.getData();
        //... Process received data ...
    }
}
