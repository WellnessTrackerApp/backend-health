package pl.edu.healthapp.service;


public interface AIService {

    String generateWeeklyAdvice(String username, String language);

    String answerHealthQuestion(String username, String question);

    String generateHealthPrediction(String username, String language);
}
