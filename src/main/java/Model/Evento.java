package Model;

public class Evento {
    private String nome;

    public Evento(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return this.getNome();
    }
}
