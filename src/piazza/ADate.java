package piazza;

public interface ADate {
	public int getMonth();
	public int getDay();
	public void setDate(int month, int day);
	/**
	 * Compares two ADate objects.<br> Note that there is no year, so this comparison assumes comparing within a single calendar year
	 * i.e. 6/8/2019 is before 3/2/2020, but (new MyDate(3, 2)).compareDate(new MyDate(6, 8)) will return -1
	 * @return -1 if the date is earlier than the argument, 1 if the date is later than the argument, 0 if the ADate objects are equal in value
	 */
	public int compareDate(ADate date);
}
