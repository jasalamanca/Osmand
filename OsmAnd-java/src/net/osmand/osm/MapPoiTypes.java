package net.osmand.osm;

import net.osmand.PlatformUtil;
import net.osmand.StringMatcher;
import net.osmand.util.Algorithms;

import org.apache.commons.logging.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class MapPoiTypes {
	private static MapPoiTypes DEFAULT_INSTANCE = null;
	private static final Log log = PlatformUtil.getLog(MapRenderingTypes.class);
	private String resourceName;
	private final List<PoiCategory> categories = new ArrayList<>();
	private PoiCategory otherCategory;
	private PoiCategory otherMapCategory;

	static final String OSM_WIKI_CATEGORY = "osmwiki";
	private PoiTranslator poiTranslator = null;
	private boolean init;
    private final Map<String, String> deprecatedTags = new LinkedHashMap<>();
	private final Map<String, String> poiAdditionalCategoryIconNames = new LinkedHashMap<>();
	private final List<PoiType> textPoiAdditionals = new ArrayList<>();


	public MapPoiTypes(String fileName) {
		this.resourceName = fileName;
	}

	public interface PoiTranslator {
		String getTranslation(AbstractPoiType type);
		String getTranslation(String keyName);
	}

	public static MapPoiTypes getDefaultNoInit() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new MapPoiTypes(null);
		}
		return DEFAULT_INSTANCE;
	}

    public static MapPoiTypes getDefault() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new MapPoiTypes(null);
			DEFAULT_INSTANCE.init();
		}
		return DEFAULT_INSTANCE;
	}

	public boolean isInit() {
		return init;
	}
	public PoiCategory getOtherPoiCategory() {
		return otherCategory;
	}

	public PoiCategory getOtherMapCategory() {
		if (otherMapCategory == null) {
			otherMapCategory = getPoiCategoryByName("Other", true);
		}
		return otherMapCategory;
	}

	public String getPoiAdditionalCategoryIconName(String category) {
		return poiAdditionalCategoryIconNames.get(category);
	}

	public List<PoiFilter> getTopVisibleFilters() {
		List<PoiFilter> lf = new ArrayList<>();
		for (PoiCategory pc : categories) {
			if (pc.isTopVisible()) {
				lf.add(pc);
			}
			for (PoiFilter p : pc.getPoiFilters()) {
				if (p.isTopVisible()) {
					lf.add(p);
				}
			}
		}
		sortList(lf);
		return lf;
	}

	private void sortList(List<? extends PoiFilter> lf) {
		final Collator instance = Collator.getInstance();
		Collections.sort(lf, (Comparator<PoiFilter>) (object1, object2) -> instance.compare(object1.getTranslation(), object2.getTranslation()));
	}

	public PoiType getPoiTypeByKey(String name) {
		for (PoiCategory pc : categories) {
			PoiType pt = pc.getPoiTypeByKeyName(name);
			if (pt != null && !pt.isReference()) {
				return pt;
			}
		}
		return null;
	}

	public PoiType getPoiTypeByKeyInCategory(PoiCategory category, String keyName) {
		if (category != null) {
			return category.getPoiTypeByKeyName(keyName);
		}
		return null;
	}

	public AbstractPoiType getAnyPoiTypeByKey(String name) {
		for (PoiCategory pc : categories) {
			if (pc.getKeyName().equals(name)) {
				return pc;
			}
			for (PoiFilter pf : pc.getPoiFilters()) {
				if (pf.getKeyName().equals(name)) {
					return pf;
				}
			}
			PoiType pt = pc.getPoiTypeByKeyName(name);
			if (pt != null && !pt.isReference()) {
				return pt;
			}
		}
		return null;
	}

	public Map<String, PoiType> getAllTranslatedNames(boolean skipNonEditable) {
		Map<String, PoiType> translation = new HashMap<>();
		for (int i = 0; i < categories.size(); i++) {
			PoiCategory pc = categories.get(i);
			if (skipNonEditable && pc.isNotEditableOsm()) {
				continue;
			}
			for (PoiType pt : pc.getPoiTypes()) {
				if (pt.isReference()) {
					continue;
				}
				if (pt.getBaseLangType() != null) {
					continue;
				}
				if (skipNonEditable && pt.isNotEditableOsm()) {
					continue;
				}
				translation.put(pt.getKeyName().replace('_', ' ').toLowerCase(), pt);
				translation.put(pt.getTranslation().toLowerCase(), pt);
			}
		}
		return translation;
	}

	public List<AbstractPoiType> getAllTypesTranslatedNames(StringMatcher matcher) {
		List<AbstractPoiType> tm = new ArrayList<>();
		for (PoiCategory pc : categories) {
			if (pc == otherMapCategory) {
				continue;
			}
			addIf(tm, pc, matcher);
			for (PoiFilter pt : pc.getPoiFilters()) {
				addIf(tm, pt, matcher);
			}
			for (PoiType pt : pc.getPoiTypes()) {
				if (pt.isReference()) {
					continue;
				}
				addIf(tm, pt, matcher);
			}
		}

		return tm;
	}

	private void addIf(List<AbstractPoiType> tm, AbstractPoiType pc, StringMatcher matcher) {
		if (matcher.matches(pc.getTranslation()) || matcher.matches(pc.getKeyName().replace('_', ' '))) {
			tm.add(pc);
		}
		List<PoiType> additionals = pc.getPoiAdditionals();
		if (additionals != null) {
			for (PoiType a : additionals) {
				addIf(tm, a, matcher);
			}
		}
	}

	public PoiCategory getPoiCategoryByName(String name) {
		return getPoiCategoryByName(name, false);
	}

	public PoiCategory getPoiCategoryByName(String name, boolean create) {
		if (name.equals("leisure") && !create) {
			name = "entertainment";
		}
		if (name.equals("historic") && !create) {
			name = "tourism";
		}
		for (PoiCategory p : categories) {
			if (p.getKeyName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		if (create) {
			PoiCategory lastCategory = new PoiCategory(this, name, categories.size());
			categories.add(lastCategory);
			return lastCategory;
		}
		return otherCategory;
	}

	public void setPoiTranslator(PoiTranslator poiTranslator) {
		this.poiTranslator = poiTranslator;
		sortList(categories);
	}

	public void init() {
		init(null);
	}

	public void init(String resourceName) {
		if (resourceName != null) {
			this.resourceName = resourceName;
		}
		try {
			InputStream is;
			if (this.resourceName == null) {
				is = MapPoiTypes.class.getResourceAsStream("poi_types.xml"); //$NON-NLS-1$
			} else {
				is = new FileInputStream(this.resourceName);
			}
			initFromInputStream(is);

		} catch (IOException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw e;
		}
	}

	private void initFromInputStream(InputStream is) {
		long time = System.currentTimeMillis();
		List<PoiType> referenceTypes = new ArrayList<>();
		final Map<String, PoiType> allTypes = new LinkedHashMap<>();
		final Map<String, List<PoiType>> categoryPoiAdditionalMap = new LinkedHashMap<>();
		final Map<AbstractPoiType, Set<String>> abstractTypeAdditionalCategories = new LinkedHashMap<>();
		this.categories.clear();
		try {
			XmlPullParser parser = PlatformUtil.newXMLPullParser();
			int tok;
			parser.setInput(is, "UTF-8");
			PoiCategory lastCategory = null;
			Set<String> lastCategoryPoiAdditionalsCategories = new TreeSet<>();
			PoiFilter lastFilter = null;
			Set<String> lastFilterPoiAdditionalsCategories = new TreeSet<>();
			PoiType lastType = null;
			Set<String> lastTypePoiAdditionalsCategories = new TreeSet<>();
			String lastPoiAdditionalCategory = null;
			while ((tok = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (tok == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if (name.equals("poi_category")) {
						lastCategory = new PoiCategory(this, parser.getAttributeValue("", "name"), categories.size());
						lastCategory.setTopVisible(Boolean.parseBoolean(parser.getAttributeValue("", "top")));
						lastCategory.setNotEditableOsm("true".equals(parser.getAttributeValue("", "no_edit")));
						lastCategory.setDefaultTag(parser.getAttributeValue("", "default_tag"));
						if(!Algorithms.isEmpty(parser.getAttributeValue("", "poi_additional_category"))) {
							Collections.addAll(lastCategoryPoiAdditionalsCategories, parser.getAttributeValue("", "poi_additional_category").split(","));
						}
						if(!Algorithms.isEmpty(parser.getAttributeValue("", "excluded_poi_additional_category"))) {
							lastCategory.addExcludedPoiAdditionalCategories(parser.getAttributeValue("", "excluded_poi_additional_category").split(","));
							lastCategoryPoiAdditionalsCategories.removeAll(lastCategory.getExcludedPoiAdditionalCategories());
						}
						categories.add(lastCategory);
					} else if (name.equals("poi_filter")) {
						PoiFilter tp = new PoiFilter(this, lastCategory, parser.getAttributeValue("", "name"));
						tp.setTopVisible(Boolean.parseBoolean(parser.getAttributeValue("", "top")));
						lastFilter = tp;
						lastFilterPoiAdditionalsCategories.addAll(lastCategoryPoiAdditionalsCategories);
						if(!Algorithms.isEmpty(parser.getAttributeValue("", "poi_additional_category"))) {
							Collections.addAll(lastFilterPoiAdditionalsCategories, parser.getAttributeValue("", "poi_additional_category").split(","));
						}
						if(!Algorithms.isEmpty(parser.getAttributeValue("", "excluded_poi_additional_category"))) {
							lastFilter.addExcludedPoiAdditionalCategories(parser.getAttributeValue("", "excluded_poi_additional_category").split(","));
							lastFilterPoiAdditionalsCategories.removeAll(lastFilter.getExcludedPoiAdditionalCategories());
						}
						lastCategory.addPoiType(tp);
					} else if (name.equals("poi_reference")) {
						PoiType tp = new PoiType(this, lastCategory, lastFilter, parser.getAttributeValue("", "name"));
						referenceTypes.add(tp);
						tp.setReferenceType(tp);
						if (lastFilter != null) {
							lastFilter.addPoiType(tp);
						}
						lastCategory.addPoiType(tp);
					} else if (name.equals("poi_additional")) {
						if (lastCategory == null) {
							lastCategory = getOtherMapCategory();
						}
						PoiType baseType = parsePoiAdditional(parser, lastCategory, lastFilter, lastType, null, null, lastPoiAdditionalCategory);
						if ("true".equals(parser.getAttributeValue("", "lang"))) {
							for (String lng : MapRenderingTypes.langs) {
								parsePoiAdditional(parser, lastCategory, lastFilter, lastType, lng, baseType, lastPoiAdditionalCategory);
							}
							parsePoiAdditional(parser, lastCategory, lastFilter, lastType, "en", baseType, lastPoiAdditionalCategory);
						}
						if (lastPoiAdditionalCategory != null) {
							List<PoiType> categoryAdditionals = categoryPoiAdditionalMap.get(lastPoiAdditionalCategory);
							if (categoryAdditionals == null) {
								categoryAdditionals = new ArrayList<>();
								categoryPoiAdditionalMap.put(lastPoiAdditionalCategory, categoryAdditionals);
							}
							categoryAdditionals.add(baseType);
						}

					} else if (name.equals("poi_additional_category")) {
						if (lastPoiAdditionalCategory == null) {
							lastPoiAdditionalCategory = parser.getAttributeValue("", "name");
							String icon = parser.getAttributeValue("", "icon");
							if (!Algorithms.isEmpty(icon)) {
								poiAdditionalCategoryIconNames.put(lastPoiAdditionalCategory, icon);
							}
						}
					} else if (name.equals("poi_type")) {
						if (lastCategory == null) {
							lastCategory = getOtherMapCategory();
						} 
						if(!Algorithms.isEmpty(parser.getAttributeValue("", "deprecated_of"))){
							String vl = parser.getAttributeValue("", "name");
							String target = parser.getAttributeValue("", "deprecated_of");
							deprecatedTags.put(vl, target);
						} else {
							lastType = parsePoiType(allTypes, parser, lastCategory, lastFilter, null, null);
							if ("true".equals(parser.getAttributeValue("", "lang"))) {
								for (String lng : MapRenderingTypes.langs) {
									parsePoiType(allTypes, parser, lastCategory, lastFilter, lng, lastType);
								}
							}
							lastTypePoiAdditionalsCategories.addAll(lastCategoryPoiAdditionalsCategories);
							lastTypePoiAdditionalsCategories.addAll(lastFilterPoiAdditionalsCategories);
							if(!Algorithms.isEmpty(parser.getAttributeValue("", "poi_additional_category"))) {
								Collections.addAll(lastTypePoiAdditionalsCategories, parser.getAttributeValue("", "poi_additional_category").split(","));
							}
							if(!Algorithms.isEmpty(parser.getAttributeValue("", "excluded_poi_additional_category"))) {
								lastType.addExcludedPoiAdditionalCategories(parser.getAttributeValue("", "excluded_poi_additional_category").split(","));
								lastTypePoiAdditionalsCategories.removeAll(lastType.getExcludedPoiAdditionalCategories());
							}
						}
					}
				} else if (tok == XmlPullParser.END_TAG) {
					String name = parser.getName();
					if (name.equals("poi_filter")) {
						if (lastFilterPoiAdditionalsCategories.size() > 0) {
							abstractTypeAdditionalCategories.put(lastFilter, lastFilterPoiAdditionalsCategories);
							lastFilterPoiAdditionalsCategories = new TreeSet<>();
						}
						lastFilter = null;
					} else if (name.equals("poi_type")) {
						if (lastTypePoiAdditionalsCategories.size() > 0) {
							abstractTypeAdditionalCategories.put(lastType, lastTypePoiAdditionalsCategories);
							lastTypePoiAdditionalsCategories = new TreeSet<>();
						}
						lastType = null;
					} else if (name.equals("poi_category")) {
						if (lastCategoryPoiAdditionalsCategories.size() > 0) {
							abstractTypeAdditionalCategories.put(lastCategory, lastCategoryPoiAdditionalsCategories);
							lastCategoryPoiAdditionalsCategories = new TreeSet<>();
						}
						lastCategory = null;
					} else if (name.equals("poi_additional_category")) {
						lastPoiAdditionalCategory = null;
					}
				}
			}
			is.close();
		} catch (IOException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw e;
		} catch (XmlPullParserException e) {
			log.error("Unexpected error", e); //$NON-NLS-1$
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		for (PoiType gt : referenceTypes) {
			PoiType pt = allTypes.get(gt.getKeyName());
			if (pt == null || pt.getOsmTag() == null) {
				throw new IllegalStateException("Can't find poi type for poi reference '" + gt.keyName + "'");
			} else {
				gt.setReferenceType(pt);
			}
		}
		for (Entry<AbstractPoiType, Set<String>> entry : abstractTypeAdditionalCategories.entrySet()) {
			for (String category : entry.getValue()) {
				List<PoiType> poiAdditionals = categoryPoiAdditionalMap.get(category);
				if (poiAdditionals != null) {
					for (PoiType poiType : poiAdditionals) {
						buildPoiAdditionalReference(poiType, entry.getKey());
					}
				}
			}
		}
		findDefaultOtherCategory();
		init = true;
		log.info("Time to init poi types " + (System.currentTimeMillis() - time)); //$NON-NLS-1$
	}

	private PoiType buildPoiAdditionalReference(PoiType poiAdditional, AbstractPoiType parent) {
		PoiCategory lastCategory = null;
		PoiFilter lastFilter = null;
		PoiType lastType = null;
		PoiType ref = null;
		if (parent instanceof PoiCategory) {
			lastCategory = (PoiCategory) parent;
			ref = new PoiType(this, lastCategory, null, poiAdditional.getKeyName());
		} else if (parent instanceof PoiFilter) {
			lastFilter = (PoiFilter) parent;
			ref = new PoiType(this, lastFilter.getPoiCategory(), lastFilter, poiAdditional.getKeyName());
		} else if (parent instanceof PoiType) {
			lastType = (PoiType) parent;
			ref = new PoiType(this, lastType.getCategory(), lastType.getFilter(), poiAdditional.getKeyName());
		}
		if (ref == null) {
			return null;
		}
		if (poiAdditional.isReference()) {
			ref.setReferenceType(poiAdditional.getReferenceType());
		} else {
			ref.setReferenceType(poiAdditional);
		}
		ref.setBaseLangType(poiAdditional.getBaseLangType());
		ref.setLang(poiAdditional.getLang());
		ref.setAdditional(lastType != null ? lastType :
				(lastFilter != null ? lastFilter : lastCategory));
		ref.setTopVisible(poiAdditional.isTopVisible());
		ref.setText(poiAdditional.isText());
		ref.setOrder(poiAdditional.getOrder());
		ref.setOsmTag(poiAdditional.getOsmTag());
		ref.setNotEditableOsm(poiAdditional.isNotEditableOsm());
		ref.setOsmValue(poiAdditional.getOsmValue());
		ref.setOsmTag2(poiAdditional.getOsmTag2());
		ref.setOsmValue2(poiAdditional.getOsmValue2());
		ref.setPoiAdditionalCategory(poiAdditional.getPoiAdditionalCategory());
		ref.setFilterOnly(poiAdditional.isFilterOnly());
		if (lastType != null) {
			lastType.addPoiAdditional(ref);
		} else if (lastFilter != null) {
			lastFilter.addPoiAdditional(ref);
		} else {
			lastCategory.addPoiAdditional(ref);
		}
		if (ref.isText()) {
			textPoiAdditionals.add(ref);
		}
		return ref;
	}

	private PoiType parsePoiAdditional(XmlPullParser parser, PoiCategory lastCategory, PoiFilter lastFilter,
			PoiType lastType, String lang, PoiType langBaseType, String poiAdditionalCategory) {
		String oname = parser.getAttributeValue("", "name");
		if (lang != null) {
			oname += ":" + lang;
		}
		String otag = parser.getAttributeValue("", "tag");
		if (lang != null) {
			otag += ":" + lang;
		}
		PoiType tp = new PoiType(this, lastCategory, lastFilter, oname);
		tp.setBaseLangType(langBaseType);
		tp.setLang(lang);
		tp.setAdditional(lastType != null ? lastType :
			 (lastFilter != null ? lastFilter : lastCategory));
		tp.setTopVisible(Boolean.parseBoolean(parser.getAttributeValue("", "top")));
		tp.setText("text".equals(parser.getAttributeValue("", "type")));
		String orderStr = parser.getAttributeValue("", "order");
		if (!Algorithms.isEmpty(orderStr)) {
			tp.setOrder(Integer.parseInt(orderStr));
		}
		tp.setOsmTag(otag);
		tp.setNotEditableOsm("true".equals(parser.getAttributeValue("", "no_edit")));
		tp.setOsmValue(parser.getAttributeValue("", "value"));
		tp.setOsmTag2(parser.getAttributeValue("", "tag2"));
		tp.setOsmValue2(parser.getAttributeValue("", "value2"));
		tp.setPoiAdditionalCategory(poiAdditionalCategory);
		tp.setFilterOnly(Boolean.parseBoolean(parser.getAttributeValue("", "filter_only")));
		if (lastType != null) {
			lastType.addPoiAdditional(tp);
		} else if (lastFilter != null) {
			lastFilter.addPoiAdditional(tp);
		} else if (lastCategory != null) {
			lastCategory.addPoiAdditional(tp);
		}
		if (tp.isText()) {
			textPoiAdditionals.add(tp);
		}
		return tp;
	}

	private PoiType parsePoiType(final Map<String, PoiType> allTypes, XmlPullParser parser, PoiCategory lastCategory,
			PoiFilter lastFilter, String lang, PoiType langBaseType) {
		String oname = parser.getAttributeValue("", "name");
		if (lang != null) {
			oname += ":" + lang;
		}
		PoiType tp = new PoiType(this, lastCategory, lastFilter, oname);
		String otag = parser.getAttributeValue("", "tag");
		if (lang != null) {
			otag += ":" + lang;
		}
		tp.setBaseLangType(langBaseType);
		tp.setLang(lang);
		tp.setOsmTag(otag);
		tp.setOsmValue(parser.getAttributeValue("", "value"));
		tp.setOsmTag2(parser.getAttributeValue("", "tag2"));
		tp.setOsmValue2(parser.getAttributeValue("", "value2"));
		tp.setText("text".equals(parser.getAttributeValue("", "type")));
		String orderStr = parser.getAttributeValue("", "order");
		if (!Algorithms.isEmpty(orderStr)) {
			tp.setOrder(Integer.parseInt(orderStr));
		}
		tp.setNameOnly("true".equals(parser.getAttributeValue("", "name_only")));
//		tp.setNameTag(parser.getAttributeValue("", "name_tag"));
		tp.setRelation("true".equals(parser.getAttributeValue("", "relation")));
		tp.setNotEditableOsm("true".equals(parser.getAttributeValue("", "no_edit")));
		if (lastFilter != null) {
			lastFilter.addPoiType(tp);
		}
		allTypes.put(tp.getKeyName(), tp);
		lastCategory.addPoiType(tp);
		if ("true".equals(parser.getAttributeValue("", "basemap"))) {
			lastCategory.addBasemapPoi(tp);
		}
		return tp;
	}

	private void findDefaultOtherCategory() {
		PoiCategory pc = getPoiCategoryByName("user_defined_other");
		if (pc == null) {
			throw new IllegalArgumentException("No poi category other");
		}
		otherCategory = pc;
	}

	public List<PoiCategory> getCategories(boolean includeMapCategory) {
		ArrayList<PoiCategory> lst = new ArrayList<>(categories);
		if (!includeMapCategory) {
			lst.remove(getOtherMapCategory());
		}
		return lst;
	}

	private PoiType getPoiAdditionalByKey(AbstractPoiType p, String name) {
		List<PoiType> pp = p.getPoiAdditionals();
		if (pp != null) {
			for (PoiType pt : pp) {
				if (pt.getKeyName().equals(name)) {
					return pt;
				}
			}
		}
		return null;
	}

	public PoiType getTextPoiAdditionalByKey(String name) {
		for (PoiType pt : textPoiAdditionals) {
			if (pt.getKeyName().equals(name)) {
				return pt;
			}
		}
		return null;
	}

	public AbstractPoiType getAnyPoiAdditionalTypeByKey(String name) {
		PoiType add = null;
		for (PoiCategory pc : categories) {
			add = getPoiAdditionalByKey(pc, name);
			if (add != null) {
				return add;
			}
			for (PoiFilter pf : pc.getPoiFilters()) {
				add = getPoiAdditionalByKey(pf, name);
				if (add != null) {
					return add;
				}
			}
			for (PoiType p : pc.getPoiTypes()) {
				add = getPoiAdditionalByKey(p, name);
				if (add != null) {
					return add;
				}
			}
		}
		return null;
	}

	public String getTranslation(AbstractPoiType abstractPoiType) {
		if (poiTranslator != null) {
			String translation = poiTranslator.getTranslation(abstractPoiType);
			if (!Algorithms.isEmpty(translation)) {
				return translation;
			}
		}
		String name = abstractPoiType.getKeyName();
		if(name.startsWith("osmand_")) {
			name = name.substring("osmand_".length());
		}
		if(name.startsWith("amenity_")) {
			name = name.substring("amenity_".length());
		}
		name = name.replace('_', ' ');
		return Algorithms.capitalizeFirstLetterAndLowercase(name);
	}

	public String getPoiTranslation(String keyName) {
		if (poiTranslator != null) {
			String translation = poiTranslator.getTranslation(keyName);
			if (!Algorithms.isEmpty(translation)) {
				return translation;
			}
		}
		String name = keyName;
		name = name.replace('_', ' ');
		return Algorithms.capitalizeFirstLetterAndLowercase(name);
	}

	public boolean isRegisteredType(PoiCategory t) {
		return getPoiCategoryByName(t.getKeyName()) != otherCategory;
	}

    public String replaceDeprecatedSubtype(String subtype) {
		if(deprecatedTags.containsKey(subtype)) {
			return deprecatedTags.get(subtype);
		}
		return subtype;
	}
}