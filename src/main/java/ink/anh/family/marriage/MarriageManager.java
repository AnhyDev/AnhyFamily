package ink.anh.family.marriage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ink.anh.family.AnhyFamily;

public class MarriageManager {

    private static MarriageManager instance;
    private Set<MarryPublic> marryList;
    private Set<MarryPrivate> proposals;

    private MarriageManager(AnhyFamily plugin) {
        marryList = new HashSet<>();
        proposals = new HashSet<>();
    }

    public static synchronized MarriageManager getInstance(AnhyFamily plugin) {
        if (instance == null) {
            instance = new MarriageManager(plugin);
        }
        return instance;
    }

    public void reload() {
        marryList.clear();
        proposals.clear();
    }

    public synchronized boolean add(Player bride1, Player bride2, Player priest, int surnameChoice, String[] chosenSurname) {
        // Перевірка, чи один з гравців вже бере участь у шлюбі
        if (contains(bride1) || contains(bride2)) {
            return false;
        }

        // Створення нового об'єкту MarryPublic
        MarryPublic marryPublic = new MarryPublic(bride1, bride2, priest, surnameChoice, chosenSurname);

        // Додавання об'єкту MarryPublic до списку
        return marryList.add(marryPublic);
    }

    public synchronized boolean contains(Object obj) {
        for (MarryPublic marryPublic : marryList) {
            if (marryPublic.isParticipant(obj)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean remove(Object obj) {
        return marryList.removeIf(marry -> marry.isParticipant(obj));
    }

    public synchronized Set<MarryPublic> getMarryList() {
        return marryList;
    }

    public synchronized MarryPublic getMarryElement(Object obj) {
        for (MarryPublic marryPublic : marryList) {
            if (marryPublic.isParticipant(obj)) {
                return marryPublic;
            }
        }
        return null;
    }

    public synchronized boolean infoMarryElement() {
        if (!marryList.isEmpty()) {
            Bukkit.getLogger().info("Заявки на свадьбу:");
            int i = 1;
            for (MarryPublic marryPublic : marryList) {
                String info = i + ". MarryPublic: " + marryPublic.getProposer().getName() + " and " + marryPublic.getReceiver().getName();
                Bukkit.getLogger().info(info);
                i++;
            }
        } else {
            Bukkit.getLogger().info("Заявки на свадьбу отсутствуют");
        }
        return true;
    }
    
    
    // Методи для пропозицій
    public synchronized boolean hasProposalConflict(MarryPrivate proposal) {
        for (MarryPrivate existingProposal : proposals) {
            if (existingProposal.equals(proposal) || 
                existingProposal.getProposer().equals(proposal.getProposer()) || 
                existingProposal.getProposer().equals(proposal.getReceiver()) || 
                existingProposal.getReceiver().equals(proposal.getProposer()) || 
                existingProposal.getReceiver().equals(proposal.getReceiver())) {
                return true;
            }
        }

        for (MarryPublic marry : marryList) {
            if (marry.isParticipant(proposal.getProposer()) || marry.isParticipant(proposal.getReceiver())) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean addProposal(MarryPrivate proposal) {
        if (hasProposalConflict(proposal)) {
            return false;
        }
        return proposals.add(proposal);
    }

    public synchronized boolean removeProposal(MarryPrivate proposal) {
        return proposals.remove(proposal);
    }

    public synchronized MarryPrivate getProposal(Player receiver) {
        for (MarryPrivate proposal : proposals) {
            if (proposal.getReceiver().equals(receiver)) {
                return proposal;
            }
        }
        return null;
    }

    public synchronized List<MarryPrivate> getProposals() {
        return new ArrayList<>(proposals);
    }
}
