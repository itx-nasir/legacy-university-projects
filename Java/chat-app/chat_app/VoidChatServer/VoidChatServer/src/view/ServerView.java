package view;

import controller.ServerController;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.User;
import model.UserFx;
import utilitez.Pair;

/**
 *
 * @author Mostafa
 */
public class ServerView extends Application implements ServerViewInt {

    public ServerController controller;
    private static ServerView instance;

    private ServerViewController serverViewController;

    public ServerView() {
        //connect to Controller
        controller = new ServerController(this);
        instance = this;
    }

    /**
     * get static instance form client view
     *
     * @return ClientView instance
     */
    public static ServerView getInstance() {
        return instance;
    }

    public void setServerViewController(ServerViewController serverViewController) {
        this.serverViewController = serverViewController;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Server.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Server");
        stage.show();
        stage.setResizable(false);
        stage.setOnCloseRequest((WindowEvent ew) -> {
            controller.loadErrorServer();
            Platform.exit();
            controller.loadErrorServer();
            //TODO : why not close
            System.exit(0);
        });
    }

    public void startServer() {

        controller.startServer();
    }

    public void stopServer() {

        controller.stopServer();
    }

    @Override
    public User getUserInfo(String username) {
        return controller.getUserInfo(username);
    }

    @Override
    public void sendAnnouncement(String message) {
        controller.sendAnnouncement(message);
    }

    @Override
    public void sendSponser(byte[] data, int dataLength) {
        controller.sendSponser(data, dataLength);
    }

    public ArrayList<User> getAllUsers() {
        if (controller.getAllUsers() != null) {
            return controller.getAllUsers();
        }
        return null;
    }

    public void updateUser(User user) {
        controller.updateUser(user);
    }

    public void GenerateUserFX(UserFx user) {
        serverViewController.data.add(user);
    }


    @Override
    public void loadErrorServer() {
        controller.loadErrorServer();
    }
    public ArrayList<Integer> getStatistics() {
        return controller.getStatistics();
    }


    public ArrayList<Pair> getGender() {
        return controller.getGender();
    }

}
