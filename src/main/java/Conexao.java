import Model.Evento;
import Model.UF;
import Service.DataBaseService;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.util.List;

public class Conexao {

    public static void main(String[] args) {
        Slack slack = Slack.getInstance();

        Dotenv dotenv = Dotenv.load();

        String idCanal = dotenv.get("CANAL");
        String token = dotenv.get("TOKEN");

        if (token == null || idCanal == null) {
            System.out.println("Erro: Variáveis de ambiente TOKEN ou CANAL não encontradas!");
            return;
        }

        DataBaseService dbService = new DataBaseService();
        List<UF> rankEstados = dbService.getUfs();
        List<Evento> proximoEvento = dbService.getProximoEvento();

        System.out.println("Estados obtidos: " + rankEstados.toString());
        System.out.println("Proximo evento: " + proximoEvento.toString());

        try {
            StringBuilder mensagem = new StringBuilder();

            mensagem.append("🎉 Um evento festivo está chegando: ").append(proximoEvento.get(0).getNome()).append("! 🎉\n\n");
            mensagem.append("Confira os melhores destinos para aproveitar esse momento:\n");

            for (int i = 0; i < rankEstados.size(); i++) {
                UF uf = rankEstados.get(i);
                mensagem.append(i + 1).append(". ").append(uf.getNome()).append("\n");
            }

            mensagem.append("\nPara mais informações, acesse nosso sistema em: http://it-insight-trip.duckdns.org/");

            String finalMensagem = mensagem.toString();

            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                    .channel(idCanal).text(finalMensagem)
            );

            if (response.isOk()) {
                System.out.println("Mensagem enviada com sucesso!");
            } else {
                System.out.println("Erro ao enviar mensagem: " + response.getError());
            }
        } catch (IOException | SlackApiException e) {
            System.out.println("Erro ao enviar mensagem para o Slack: " + e);
        }
    }
}