package pl.edu.healthapp.service;


public interface AIService {

    String generateWeeklyAdvice(String username);

    String answerHealthQuestion(String username, String question);

    String generateHealthPrediction(String username);
}
