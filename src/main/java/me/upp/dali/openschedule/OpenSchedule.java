package me.upp.dali.openschedule;

import it.auties.whatsapp4j.manager.WhatsappDataManager;
import it.auties.whatsapp4j.whatsapp.WhatsappAPI;
import javafx.application.Application;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.upp.dali.openschedule.controller.MessagesAPI;
import me.upp.dali.openschedule.controller.handlers.MessagesHandler;
import me.upp.dali.openschedule.model.Connector;
import me.upp.dali.openschedule.model.database.Database;
import me.upp.dali.openschedule.model.database.DatabaseConnector;
import me.upp.dali.openschedule.model.database.SQLite;
import me.upp.dali.openschedule.view.ViewLoader;

/**
 * @author Dali
 *
 * Open Schedule main class
 */
@Getter @Setter
public class OpenSchedule {

    private Connector connector;
    private Database database;
    private MessagesAPI messagesAPI;

    private static OpenSchedule INSTANCE;

    public static @NonNull OpenSchedule getINSTANCE() {
        return INSTANCE;
    }

    public static void main(final String[] args) {
        INSTANCE = new OpenSchedule();

        // Interfaces loader
        new Thread(() -> Application.launch(ViewLoader.class, args)).start();
        System.out.println("Loading...");

        // WhatsApp connector
        final MessagesAPI messagesAPI = new MessagesAPI();
        final WhatsappAPI whatsappAPI = messagesAPI.getWhatsappAPI();
        final WhatsappDataManager manager = whatsappAPI.manager();
        whatsappAPI.registerListener(new MessagesHandler(manager, whatsappAPI));
        whatsappAPI.connect();

        // Database creation
        final Connector connector = new DatabaseConnector();
        final Database database = new SQLite(connector);
        INSTANCE.createTables(database);

        // Singleton instantiation
        INSTANCE.setConnector(connector);
        INSTANCE.setDatabase(database);
        INSTANCE.setMessagesAPI(messagesAPI);
    }

    public void createTables(final Database database) {
        database.createTable("tbl_user", "userId INTEGER NOT NULL", "name TEXT NOT NULL", "phone TEXT NOT NULL", "PRIMARY KEY(\"userId\" AUTOINCREMENT)");
        database.createTable("tbl_user_time", "userTimeId INTEGER NOT NULL", "phone TEXT NOT NULL", "userCode INTEGER NOT NULL",
                "tiempoInicio DATE DEFAULT (datetime('now', 'localtime'))","tiempoFin DATE", "PRIMARY KEY(\"userTimeId\" AUTOINCREMENT)");
        database.createTable("tbl_config", "configId INTEGER NOT NULL", "id TEXT NOT NULL", "value TEXT NOT NULL", "PRIMARY KEY(\"configId\" AUTOINCREMENT)");
    }
}