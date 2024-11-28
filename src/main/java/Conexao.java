import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;

public class Conexao {
    public static void main(String[] args) throws SlackApiException, IOException {
        Slack slack = Slack.getInstance();

        String mensagem = "Sistema rodando";
        Dotenv dotenv = Dotenv.load();

        String idCanal = dotenv.get("CANAL");
        String token = dotenv.get("TOKEN");

        if (token == null || idCanal == null) {
            System.err.println("Erro: Variáveis de ambiente TOKEN ou CANAL não encontradas!");
            return;
        }

        try {
            String finalMensagem = mensagem;

            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                    .channel(idCanal).text(finalMensagem)
            );

                    if (response.isOk()) {
                        System.out.println("Mensagem enviada com sucesso!");
                    } else {
                        System.out.printf("Erro ao enviar mensagem: %s%n", response.getError());
                    }
                } catch (IOException | SlackApiException e) {
                    e.printStackTrace();
                }
            }
}

