package model;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import util.LabelServiceFixed;

public class Model {
	
	public static final String MILESTONES_ALL = "all";
	public static final String MILESTONES_OPEN = "open";
	public static final String MILESTONES_CLOSED = "closed";
	
	private ObservableList<TurboCollaborator> collaborators;
	private ObservableList<TurboIssue> issues;
	private ObservableList<TurboLabel> labels;
	private ObservableList<TurboMilestone> milestones;

	private IRepositoryIdProvider repoId;
	
	private CollaboratorService collabService;
	private IssueService issueService;
	private LabelServiceFixed labelService;
	private MilestoneService milestoneService;

	public Model(GitHubClient ghClient) {
		this.collabService = new CollaboratorService(ghClient);
		this.issueService = new IssueService(ghClient);
		this.labelService = new LabelServiceFixed(ghClient);
		this.milestoneService = new MilestoneService(ghClient);
	}
	
	public void setRepoId(String owner, String name) {
		repoId = RepositoryId.create(owner, name);
		loadCollaborators();
		loadIssues();
		loadLabels();
		loadMilestones();
	}

	public ObservableList<TurboIssue> getIssues() {
		return issues;
	}
	
	public ObservableList<TurboCollaborator> getCollaborators() {
		return collaborators;
	}

	public ObservableList<TurboLabel> getLabels() {
		return labels;
	}

	public ObservableList<TurboMilestone> getMilestones() {
		return milestones;
	}
	
	private boolean loadCollaborators() {
		if (collaborators == null) {
			collaborators = FXCollections.observableArrayList();
		} else {
			collaborators.clear();
		}
		try {
			List<User> ghCollaborators = collabService.getCollaborators(repoId);
			for(User ghCollaborator : ghCollaborators) {
				collaborators.add(new TurboCollaborator(ghCollaborator));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadIssues() {
		if (issues == null) {
			issues = FXCollections.observableArrayList();
		} else {
			issues.clear();
		}
		Map<String, String> filters = new HashMap<String, String>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		try {		
			List<Issue> ghIssues = issueService.getIssues(repoId, filters);
			for (Issue ghIssue : ghIssues) {
				issues.add(new TurboIssue(ghIssue));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadLabels(){
		if (labels == null) {
			labels = FXCollections.observableArrayList();
		} else {
			labels.clear();
		}
		try {
			List<Label> ghLabels = labelService.getLabels(repoId);
			for (Label ghLabel : ghLabels) {
				labels.add(new TurboLabel(ghLabel));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadMilestones(){
		if (milestones == null) {
			milestones = FXCollections.observableArrayList();
		} else {
			milestones.clear();
		}
		try {		
			List<Milestone> ghMilestones = milestoneService.getMilestones(repoId, MILESTONES_ALL);
			for (Milestone ghMilestone : ghMilestones) {
				milestones.add(new TurboMilestone(ghMilestone));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public TurboIssue createIssue(TurboIssue newIssue) {
		Issue ghIssue = newIssue.toGhIssue();
		Issue createdIssue = null;
		try {
			createdIssue = issueService.createIssue(repoId, ghIssue);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue);
		issues.add(returnedIssue);
		return returnedIssue;
	}
	
	public TurboLabel createLabel(TurboLabel newLabel) {
		Label ghLabel = newLabel.toGhLabel();
		Label createdLabel = null;
		try {
			createdLabel = labelService.createLabel(repoId, ghLabel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TurboLabel returnedLabel = new TurboLabel(createdLabel);
		labels.add(returnedLabel);
		return returnedLabel;
	}
	
	public TurboMilestone createMilestone(TurboMilestone newMilestone) {
		Milestone ghMilestone = newMilestone.toGhMilestone();
		Milestone createdMilestone = null;
		try {
			createdMilestone = milestoneService.createMilestone(repoId, ghMilestone);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboMilestone returnedMilestone = new TurboMilestone(createdMilestone);
		milestones.add(returnedMilestone);
		return returnedMilestone;
	}
	
	public void deleteLabel(TurboLabel label) {
		Label ghLabel = label.toGhLabel();
		try {
			labelService.deleteLabel(repoId, URLEncoder.encode(ghLabel.getName(), "ISO-8859-1"));
			labels.remove(label);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteMilestone(TurboMilestone milestone) {
		try {
			milestoneService.deleteMilestone(repoId, milestone.getNumber());
			milestones.remove(milestone);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateIssue(TurboIssue orignalIssue, TurboIssue editedIssue) {
		Issue original = orignalIssue.toGhIssue();
		Issue edited = editedIssue.toGhIssue();
		try {
			Issue latest = issueService.getIssue(repoId, editedIssue.getId());
			
			String originalTitle = original.getTitle();
			String editedTitle = edited.getTitle();
			if (!editedTitle.equals(originalTitle)) {latest.setTitle(editedTitle);}
			
			String originalBody = original.getBody();
			String editedBody = edited.getBody();
			if (!editedBody.equals(originalBody)) {latest.setBody(editedBody);}
			
			User originalAssignee = original.getAssignee();
			User editedAssignee = edited.getAssignee();
			String originalALogin = (originalAssignee != null) ? originalAssignee.getLogin() : "";
			String editedALogin = (editedAssignee != null) ? editedAssignee.getLogin() : "";
			if (!editedALogin.equals(originalALogin)) {
				// this check is for cleared assignee
				if (editedAssignee == null) {
					latest.setAssignee(new User());
				} else {
					latest.setAssignee(editedAssignee);
				}
			}
			
			String originalState = original.getState();
			String editedState = edited.getState();
			if (!editedState.equals(originalState)) {latest.setState(editedState);}
			
			
			Milestone originalMilestone = original.getMilestone();
			Milestone editedMilestone = edited.getMilestone();
			int originalMNumber = (originalMilestone != null) ? originalMilestone.getNumber() : 0;
			int editedMNumber = (editedMilestone != null) ? editedMilestone.getNumber() : 0;
			if (editedMNumber != originalMNumber) {
				// this check is for cleared milestone
				if (editedMilestone == null) {
					latest.setMilestone(new Milestone());
				} else {
					latest.setMilestone(editedMilestone);
				}
			}
			
			List<Label> originalLabels = original.getLabels();
			List<Label> editedLabels = edited.getLabels();
			boolean isSameLabels = true;
			for (Label editedLabel : editedLabels) {
				if (!originalLabels.contains(editedLabel)) {
					isSameLabels = false;
					break;
				}
			}
			if (!isSameLabels) {latest.setLabels(editedLabels);}

			issueService.editIssue(repoId, latest);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void updateLabel(TurboLabel editedLabel, String labelName) {
		Label ghLabel = editedLabel.toGhLabel();
		try {
			labelService.editLabel(repoId, ghLabel, labelName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateMilestone(TurboMilestone editedMilestone) {
		Milestone ghMilestone = editedMilestone.toGhMilestone();
		try {
			milestoneService.editMilestone(repoId, ghMilestone);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
