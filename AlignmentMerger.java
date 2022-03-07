/******************************************************************************
* Merges an ontology with all its imports, which can be used to merge two     *
* aligned ontologies through their OWL alignment, or merges a set of          *
* ontologies using their RDF or OWL alignments.                               *
*                                                                             *
* @author Marta Silva, Daniel Faria                                           *
******************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.parameters.Imports;

public class AlignmentMerger
{
	/**
	 * Merges an ontology with all its imports and stores it in a new file
	 * @param in: the location of the input ontology
	 * @param out: the file path to save the merged ontology
	 * @throws OWLOntologyStorageException
	 * @throws FileNotFoundException
	 * @throws OWLOntologyCreationException
	 */
	public static void mergeImports(String in, String out) throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException
	{	
		System.out.println("Starting AlignmentMerger in single alignment mode");
		//Start and configure OWL ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		manager.setOntologyLoaderConfiguration(conf);
		File f_out = new File(out);
		//Load input ontology
		OWLOntology o_in = manager.loadOntologyFromOntologyDocument(new File(in));
		//Remove the SKOS import if present
		manager.applyChange(new RemoveImport(o_in, manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.w3.org/2004/02/skos/core#"))));
		//Create output ontology with the same IRI as the input ontology or alternatively the IRI of the input ontology file
		OWLOntology o_out = manager.createOntology(IRI.create(f_out));
		//Copy all axioms from input to output
		o_in.axioms(Imports.INCLUDED).forEach(ax -> o_out.addAxioms(ax));
		//Save merged ontology in the same format as the input ontology
		manager.saveOntology(o_out, manager.getOntologyFormat(o_in), IRI.create(f_out));
		System.out.println("Finished! Saved merged ontology in " + out);
	}
	
	/**
	 * Merges a set of ontologies in a single file
	 * @param ontologies: the set of ontologies to merge
	 * @param out: the path to the file to save the merged ontology
	 * @throws OWLOntologyStorageException
	 * @throws FileNotFoundException
	 * @throws OWLOntologyCreationException
	 */
	public static void mergeOntologies(Set<String> ontologies, String out) throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException
	{	
		System.out.println("Starting AlignmentMerger in set mode");
		//Start and configure OWL ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		manager.setOntologyLoaderConfiguration(conf);
		OWLOntology o_out = manager.createOntology(IRI.create(new File(out)));
		for(String o : ontologies)
		{
			//Load input ontology
			OWLOntology o_in = manager.loadOntologyFromOntologyDocument(new File(o));
			//Remove the SKOS import if present
			manager.applyChange(new RemoveImport(o_in, manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.w3.org/2004/02/skos/core#"))));
			//Copy all axioms from input to output
			o_in.axioms(Imports.INCLUDED).forEach(ax -> o_out.addAxioms(ax));
		}
		//Save merged ontology in the same format as the input ontology
		manager.saveOntology(o_out, IRI.create(new File(out)));
		System.out.println("Finished! Saved merged ontologies in " + out);
	}
}
