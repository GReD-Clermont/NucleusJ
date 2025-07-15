package fr.igred.nucleus.dialogs;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;

import java.util.concurrent.ExecutionException;


@FunctionalInterface
public interface IDialogListener {
	void onStart() throws AccessException, ServiceException, ExecutionException;
	
}
