/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nametagautoprint;


import java.io.IOException;
import java.util.*;

/**
 *
 * @author tim
 */
public class PrintMaster {
    
    private static List<Printer> printers = new ArrayList<>();
    private static List<Nametag> queue = new ArrayList<>();
    private static int position = 0;
    
    public static void addPrinter(Printer printer){
        printers.add(printer);
        NametagAutoPrint.getInstance().printersController.getPrinterPanes().add(printer.getPane());
        System.out.println("Added Printer: " + printer);
        System.out.println("All Printers: " + printers);
    }
    
    public static void addAllPrinters(Collection<Printer> newPrinters){
        System.out.println("New Printers: " + newPrinters);
        printers.addAll(newPrinters);
        newPrinters.forEach(p -> {
            NametagAutoPrint.getInstance().printersController.getPrinterPanes().add(p.getPane());
            System.out.println(NametagAutoPrint.getInstance().printersController);
        System.out.println("Added Printer: " + p);
        });
        System.out.println("Added Printers: " + newPrinters);
        System.out.println("All Printers: " + printers);
    }
    
    public static void removePrinter(Printer printer){
        printers.remove(printer);
        NametagAutoPrint.getInstance().printersController.getPrinterPanes().remove(printer.getPane());
        System.out.println("Removed Printer: " + printer);
        System.out.println("All Printers: " + printers);
    }

    public static Printer getNextPrinter() {
        int i = position, checked = 0;
        if(printers.size() == 0)
            return null;
        for(;;) {
            if(printers.get(i).isActive() && printers.get(i).isAvailable())
                return printers.get(i);
            checked++;
            if(checked == printers.size())
                return null;
            if(i >= printers.size())
                i = 0;
            else
                i++;
        }
    }

    public static void addToQueue(Nametag tag) throws IOException {
        queue.add(tag);
        XML.saveQueue();
        NametagAutoPrint.getInstance().queueController.getPrinterPanes().add(tag.getPane());
        System.out.println("Added Nametag to queue: " + tag);
        System.out.println("Queue: " + queue);
    }
    
    public static void addAllToQueue(Collection<Nametag> nameTags) throws IOException {
        System.out.println("New Printers: " + nameTags);
        queue.addAll(nameTags);
        XML.saveQueue();
        nameTags.forEach(p -> {
            NametagAutoPrint.getInstance().queueController.getPrinterPanes().add(p.getPane());
            System.out.println(NametagAutoPrint.getInstance().queueController);
            System.out.println("Added Nametag to queue: " + p);
        });
        System.out.println("Added to queue: " + nameTags);
        System.out.println("Queue: " + queue);
    }
    
    public static void removeFromQueue(Nametag tag) throws IOException {
        queue.remove(tag);
        XML.saveQueue();
        NametagAutoPrint.getInstance().queueController.getPrinterPanes().remove(tag.getPane());
        System.out.println("Removed Nametag from queue: " + tag);
        System.out.println("Queue: " + printers);
    }

    public static Nametag getNextNameToSlicer() {
        for(int i = 0; i < queue.size() && queue.size() > 0; i++) {
            if(!queue.get(i).isSliced() && queue.get(i).isGenerated())
                return queue.get(i);
        }
        return null;
    }

    public static Nametag getNextNameToUpload() {
        for(int i = 0; i < queue.size() && queue.size() > 0; i++) {
            if(queue.get(i).isSliced() && queue.get(i).isGenerated())
                return queue.get(i);
        }
        return null;
    }

    public static Printer getPrinter(int i) {
        return printers.get(i);
    }

    public static Printer getPrinter(String name) {
        for(Printer printer : printers) {
            if(printer.getName().equals(name))
                return printer;
        }
        return null;
    }

    public static List<Printer> getAllPrinters() {
        return printers;
    }

    public static List<Nametag> getAllNametags() {
        return queue;
    }

    public static Iterator getQueueIterator() {
        return queue.iterator();
    }
}
