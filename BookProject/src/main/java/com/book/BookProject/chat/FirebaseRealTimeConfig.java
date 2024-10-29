package com.book.BookProject.chat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseRealTimeConfig {

  @Bean
  public FirebaseDatabase firebaseDatabase() throws IOException {
      FileInputStream serviceAccount =
          new FileInputStream("src/main/resources/ChatServiceAccountKey.json");

      FirebaseOptions options = new FirebaseOptions.Builder()
    		  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
    		  .setDatabaseUrl("https://springbootstudy2-default-rtdb.asia-southeast1.firebasedatabase.app")
    		  .build();

      if (FirebaseApp.getApps().isEmpty()) {
          FirebaseApp.initializeApp(options);
      }

      return FirebaseDatabase.getInstance();
  }

  @Bean
  public DatabaseReference databaseReference(FirebaseDatabase firebaseDatabase) {
      return firebaseDatabase.getReference("messages");
  }
}
