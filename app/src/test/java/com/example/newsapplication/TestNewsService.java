

import java.util.List;
import java.util.Properties;
import java.io.IOException;

import es.upm.hcid.pui.assignment.Article;
import es.upm.hcid.pui.assignment.Image;
import es.upm.hcid.pui.assignment.ModelManager;
import es.upm.hcid.pui.assignment.Utils;
import es.upm.hcid.pui.assignment.exceptions.AuthenticationError;
import es.upm.hcid.pui.assignment.exceptions.ServerCommunicationError;

public class TestNewsService {


	public static void main(String[] args) throws AuthenticationError, ServerCommunicationError, IOException {		
		
		Properties prop = new Properties();
		prop.setProperty(ModelManager.ATTR_LOGIN_USER, "XXXX");
		prop.setProperty(ModelManager.ATTR_LOGIN_PASS, "XXXX");
		prop.setProperty(ModelManager.ATTR_SERVICE_URL, "https://sanger.dia.fi.upm.es/pui-rest-news/");
		prop.setProperty(ModelManager.ATTR_REQUIRE_SELF_CERT, "TRUE");
		
		// Log in
		ModelManager mm = null;
		try{
		
			mm = new ModelManager(prop);
		}catch (AuthenticationError e) {
			
			System.exit(-1);
		}
		
		// get list of articñes for logged user
		List<Article> res = mm.getArticles(2,2);
		for (Article article : res) {
			System.out.println(article);
		}
		
		// create one article and save in server 
		/*
		Article myArticle = null;
		myArticle = new Article(mm, "Categoria","Titulo","Resumen articulo mio", "Cuerpo de texto", "Pie del articulo", true, new java.util.Date());
		myArticle.save();
		*/
		
		// save one image to an article (through the article service)
		/*
		myArticle.addImage(Utils.encodeImage(Utils.captureScreen()), "escritorio");
		myArticle.save();
		*/
		
		// save one image to an article (through image service)
		// Utils.captureScreen() doesn't exists in mobile, should use Utils methods from one Bitmap
		/*
		Image ii = myArticle.addImage( Utils.captureScreen(), "testing image "+ lIm.size()+1);
		ii.save();
		ii.delete();
		*/
		

	}
}
