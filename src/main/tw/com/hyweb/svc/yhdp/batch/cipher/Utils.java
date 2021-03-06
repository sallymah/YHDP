package tw.com.hyweb.svc.yhdp.batch.cipher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;

public class Utils {

	private static Logger log = Logger.getLogger(Utils.class);

	private static final String MEMBER_GROUP_REPLACE_PATTERN = ".*2{5}.*";
	private static final String MEMBER_GROUP_REPLACE_MARK_PATTERN = "22222";
	private static final String MEMBER_REPLACE_PATTERN = ".*0{8}.*";
	private static final String MEMBER_REPLACE_MARK_PATTERN = "00000000";
	private static final String MERCH_REPLACE_PATTERN = ".*1{15}.*";
	private static final String MERCH_REPLACE_MARK_PATTERN = "111111111111111";

	public Utils() {
	}

	public static List setMatchFiles(String tempDir, TbFileInfoInfo fileInfo, List jobMemIds)
			throws Exception {

		List matchFiles = new ArrayList();
		String InnermostPath = "";
		if (fileInfo.getLocalPath().matches(MEMBER_GROUP_REPLACE_PATTERN)) {
			String parentLocalPath = FilenameUtils
					.separatorsToSystem((tempDir + '/')
							+ (fileInfo.getLocalPath().substring(
									0,
									fileInfo.getLocalPath().indexOf(
											MEMBER_GROUP_REPLACE_MARK_PATTERN)) + '/'));
			InnermostPath = fileInfo.getLocalPath().substring(
					fileInfo.getLocalPath().indexOf(
							MEMBER_GROUP_REPLACE_MARK_PATTERN)
							+ MEMBER_GROUP_REPLACE_MARK_PATTERN.length())
					+ "/";
			File parentLocaldir = new File(parentLocalPath);
			if (parentLocaldir.listFiles() != null) {
				for (File memberGroupfile : parentLocaldir.listFiles()) {
					if (fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)) {
						String memberGrouptLocalPath = parentLocalPath
								+ memberGroupfile.getName() + "/";
						InnermostPath = fileInfo.getLocalPath().substring(
								fileInfo.getLocalPath().indexOf(
										MEMBER_REPLACE_MARK_PATTERN)
										+ MEMBER_REPLACE_MARK_PATTERN.length())
								+ "/";
						File memberGrouptLocaldir = new File(
								memberGrouptLocalPath);
						if (memberGrouptLocaldir.listFiles() != null) {
							for (File memberfile : memberGrouptLocaldir
									.listFiles()) {
								
								if (jobMemIds != null){
									if (!jobMemIds.contains(memberfile.getName())){
										continue;
									}
								}
								
								if (fileInfo.getLocalPath().matches(
										MERCH_REPLACE_PATTERN)) {
									String memberLocalPath = memberGrouptLocalPath
											+ memberfile.getName() + "/";
									InnermostPath = fileInfo
											.getLocalPath()
											.substring(
													fileInfo.getLocalPath()
															.indexOf(
																	MERCH_REPLACE_MARK_PATTERN)
															+ MERCH_REPLACE_MARK_PATTERN
																	.length())
											+ "/";
									File memberLocaldir = new File(
											memberLocalPath);
									if (memberLocaldir.listFiles() != null) {
										for (File merchfile : memberLocaldir
												.listFiles()) {
											merchfile = new File(
													merchfile.getAbsoluteFile()
															+ "/"
															+ InnermostPath);
											if (merchfile.isDirectory()
													&& merchfile.listFiles() != null) {
												for (File ls : merchfile
														.listFiles()) {
													log.info("ls:"
															+ ls.getAbsolutePath());
													if (ls.isFile()
															&& isMatchFile(
																	ls.getName(),
																	fileInfo)) {
														if (hasOKFile(ls,
																fileInfo)) {
															matchFiles.add(ls);
														}
													}
												}
											} else
												log.info("ls is null!");
										}
									} else
										log.info(memberLocalPath + " is null!");
								} else {
									memberfile = new File(
											memberfile.getAbsoluteFile() + "/"
													+ InnermostPath);
									if (memberfile.isDirectory()
											&& memberfile.listFiles() != null) {
										for (File ls : memberfile.listFiles()) {
											log.info("ls:"
													+ ls.getAbsolutePath());
											if (ls.isFile()
													&& isMatchFile(
															ls.getName(),
															fileInfo)) {
												if (hasOKFile(ls, fileInfo)) {
													matchFiles.add(ls);
												}
											}
										}
									}
								}
							}
						} else
							log.info(memberGrouptLocalPath + " is null!");
					} else {
						memberGroupfile = new File(
								memberGroupfile.getAbsoluteFile() + "/"
										+ InnermostPath);
						if (memberGroupfile.isDirectory()
								&& memberGroupfile.listFiles() != null) {
							for (File ls : memberGroupfile.listFiles()) {
								log.info("ls:" + ls.getAbsolutePath());
								if (ls.isFile()
										&& isMatchFile(ls.getName(), fileInfo)) {
									if (hasOKFile(ls, fileInfo)) {
										matchFiles.add(ls);
									}
								}
							}
						}
					}
				}
			} else
				log.info(parentLocalPath + " is null!");

		} else if (fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
				&& !fileInfo.getLocalPath().matches(
						MEMBER_GROUP_REPLACE_PATTERN)) {

			String parentLocalPath = FilenameUtils
					.separatorsToSystem((tempDir + '/')
							+ (fileInfo.getLocalPath().substring(
									0,
									fileInfo.getLocalPath().indexOf(
											MEMBER_REPLACE_MARK_PATTERN)) + '/'));
			InnermostPath = fileInfo.getLocalPath().substring(
					fileInfo.getLocalPath()
							.indexOf(MEMBER_REPLACE_MARK_PATTERN)
							+ MEMBER_REPLACE_MARK_PATTERN.length())
					+ "/";
			File parentLocaldir = new File(parentLocalPath);

			if (parentLocaldir.listFiles() != null) {
				for (File memberfile : parentLocaldir.listFiles()) {
					
					if (jobMemIds != null){
						if (!jobMemIds.contains(memberfile.getName())){
							continue;
						}
					}
					
					if (fileInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)) {
						String memberLocalPath = parentLocaldir
								+ memberfile.getName() + "/";
						InnermostPath = fileInfo.getLocalPath().substring(
								fileInfo.getLocalPath().indexOf(
										MERCH_REPLACE_MARK_PATTERN)
										+ MERCH_REPLACE_MARK_PATTERN.length())
								+ "/";
						File memberLocaldir = new File(memberLocalPath);
						if (memberLocaldir.listFiles() != null) {
							for (File merchfile : memberLocaldir.listFiles()) {
								merchfile = new File(
										merchfile.getAbsoluteFile() + "/"
												+ InnermostPath);
								if (merchfile.isDirectory()
										&& merchfile.listFiles() != null) {
									for (File ls : merchfile.listFiles()) {
										log.info("ls:" + ls.getAbsolutePath());
										if (ls.isFile()
												&& isMatchFile(ls.getName(),
														fileInfo)) {
											if (hasOKFile(ls, fileInfo)) {
												matchFiles.add(ls);
											}
										}
									}
								}
							}
						}
					} else {
						memberfile = new File(memberfile.getAbsoluteFile()
								+ InnermostPath);
						if (memberfile.isDirectory()
								&& memberfile.listFiles() != null) {
							for (File ls : memberfile.listFiles()) {
								log.info("ls:" + ls.getAbsolutePath());
								if (ls.isFile()
										&& isMatchFile(ls.getName(), fileInfo)) {
									if (hasOKFile(ls, fileInfo)) {
										matchFiles.add(ls);
									}
								}
							}
						}
					}
				}
			}
		} else if (fileInfo.getLocalPath().matches(MERCH_REPLACE_PATTERN)
				&& !fileInfo.getLocalPath().matches(MEMBER_REPLACE_PATTERN)
				&& !fileInfo.getLocalPath().matches(
						MEMBER_GROUP_REPLACE_PATTERN)) {

			String parentLocalPath = FilenameUtils
					.separatorsToSystem((tempDir + '/')
							+ (fileInfo.getLocalPath().substring(
									0,
									fileInfo.getLocalPath().indexOf(
											MERCH_REPLACE_MARK_PATTERN)) + '/'));
			InnermostPath = fileInfo.getLocalPath().substring(
					fileInfo.getLocalPath().indexOf(MERCH_REPLACE_MARK_PATTERN)
							+ MERCH_REPLACE_MARK_PATTERN.length())
					+ "/";
			File parentLocaldir = new File(parentLocalPath + InnermostPath);

			if (parentLocaldir.listFiles() != null) {
				for (File merchfile : parentLocaldir.listFiles()) {
					if (merchfile.isDirectory()
							&& merchfile.listFiles() != null) {
						for (File ls : merchfile.listFiles()) {
							log.info("ls:" + ls.getAbsolutePath());
							if (ls.isFile()
									&& isMatchFile(ls.getName(), fileInfo)) {
								if (hasOKFile(ls, fileInfo)) {
									matchFiles.add(ls);
								}
							}
						}
					}
				}
			}
		} else {
			String parentLocalPath = FilenameUtils
					.separatorsToSystem((tempDir + '/')
							+ (fileInfo.getLocalPath() + '/'));
			File parentLocaldir = new File(parentLocalPath);
			if (parentLocaldir.listFiles() != null) {
				for (File ls : parentLocaldir.listFiles()) {
					log.info("ls:" + ls.getAbsolutePath());
					if (ls.isFile() && isMatchFile(ls.getName(), fileInfo)) {
						if (hasOKFile(ls, fileInfo)) {
							matchFiles.add(ls);
						}
					}
				}
			} else
				log.info("ls is null!");
		}
		log.info("matchFiles.size: " + matchFiles.size());
		return matchFiles;
	}

	private static boolean isMatchFile(String fn, TbFileInfoInfo fileInfo) {
		boolean ret = false;
		Pattern p = Pattern.compile(fileInfo.getFileNamePattern());
		Matcher m = p.matcher(fn);
		ret = m.matches();
		log.info("fn:" + fn + " matched:" + ret);
		return ret;
	}

	private static boolean hasOKFile(File f, TbFileInfoInfo fileInfo) {
		if (Layer1Constants.OKFLAG_NOCHECK.equals(fileInfo.getOkFlag())) {
			// ???????????? OK file
			return true;
		} else if (Layer1Constants.OKFLAG_CHECK.equals(fileInfo.getOkFlag())) {
			// ????????? OK file
			String subFileName = "";
			String file = "";
			if (fileInfo.getSubFileName().contains("/")) {
				subFileName = fileInfo.getSubFileName().replaceAll("/", "");
				file = f.getAbsolutePath().substring(0,
						f.getAbsolutePath().lastIndexOf("."))
						+ subFileName;
			} else {
				subFileName = fileInfo.getSubFileName();
				file = f.getAbsolutePath() + subFileName;
			}
			File ok = new File(file);
			if (ok.isFile() && ok.exists()) {
				return true;
			} else {
				return false;
			}
		} else {
			// unknown, suppose ???????????? OK file
			log.warn("unknown OK_FLAG(" + fileInfo.getOkFlag()
					+ ")! suppose no check OK file!");
			return true;
		}
	}

	public static String formatPath(String path) {
		path = FilenameUtils.separatorsToSystem(path);
		if (!path.endsWith(File.separator))
			path += File.separator;
		path = normalFileSeparator(path);
		return path;
	}

	public static String normalFileSeparator(String fn) {
		return fn.replace('\\', '/');
	}

	public static String checkMark(String mark, String replaceMark) {
		if (mark == null || mark.length() == 0) {
			return replaceMark;
		}
		if (mark.contains("/") || mark.contains("\\")) {
			return replaceMark;
		}
		if (!mark.startsWith(".") && !mark.startsWith("_")
				&& !mark.startsWith("-")) {
			return "." + mark;
		}
		return mark;
	}

	public static boolean getPinKeyId(String name) {
		if (name == null || "".equals(name) || name.length() == 0) {
			return false;
		}
		String[] args = name.split("\\.");
		if (args.length < 3) {
			return false;
		}
		if ("00078067".equals(args[1])) {
			return false;
		}
		return true;
	}
}