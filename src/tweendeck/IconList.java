package tweendeck;

import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ReplicateScaleFilter;
import java.util.HashMap;

import javax.swing.ImageIcon;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.User;

/*
 * アイコンの管理を行う
 * ・getImageIconでImageIconオブジェクトを取得できる
 * ・アイコン取得要求を受け付けた際
 * 		・まだダウンロードしていないアイコンであった場合
 * 			getProfileURLにてアイコンのURLを取得し、ImageIconオブジェクトを作成する
 * 			作成したImageIconオブジェクトはユーザ名をkeyとするHashMapに登録する
 * 		・既にダウンロード済みの場合
 * 			HashMapからImageIconオブジェクトを取り出す。
 * 
 * 既知の不具合
 * 	・アイコンがアニメgifの場合、何も描画されない
 * 
 * */
class IconList{
	HashMap<String, ImageIcon> list;

	public IconList(){

		try{
			list = new HashMap<String, ImageIcon>();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public ImageIcon getImageIcon(User user){
		ImageIcon tmp;
		if(list.containsKey(user.getName())){
			System.out.println(user.getName() + "'s icon has already got. returning cache.");
			return list.get(user.getName());
		}
		else{
			String fileSuffix = getSuffix(user.getProfileImageURL().toString());
			if(fileSuffix.equals("gif")){
				System.out.println(user.getName() + "'s icon is gif!! this is not supported!!");

				/*
				tmp = new ImageIcon("./src/images/gif.jpg");
				Image gifImage = tmp.getImage();
				 */
				/*
        Iterator<ImageReader> itr = ImageIO.getImageReadersByFormatName("gif");
        ImageReader reader = null;
        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

        if(itr.hasNext()) reader = itr.next();
        else throw new RuntimeException();

        reader.setInput(ImageIO.createImageInputStream(new File(arg[0])));

        int count = reader.getNumImages(true);
        for(int i = 0;i < count ;i++) {
            images.add(reader.read(i));
        }

				 */

				tmp = new ImageIcon(user.getProfileImageURL());
				ImageFilter fl = new ReplicateScaleFilter(50,50);
				FilteredImageSource fis = null;
				try{
					fis = new FilteredImageSource(tmp.getImage().getSource(), fl);
				}catch (Exception ex){
					System.out.println("fis error");
					ex.printStackTrace();		        	
				}
				tmp = new ImageIcon(Toolkit.getDefaultToolkit().createImage(fis));
			}
			else{
				System.out.print("getting Icon "+ user.getProfileImageURL() + "...");
				tmp = new ImageIcon(user.getProfileImageURL());
				System.out.println(" done");
			}
			list.put(user.getName(),tmp);
			return tmp;
		}
	}
	private String getSuffix(String fileName){
		if (fileName == null)
			return null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			return fileName.substring(point + 1);
		}
		return null;

	}
}
