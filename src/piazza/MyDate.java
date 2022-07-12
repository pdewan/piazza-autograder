package piazza;

public class MyDate implements ADate {

	private int month;
	private int day;
	
	public MyDate(int month, int day) {
		this.month = month;
		this.day = day;
	}
	
	@Override
	public void setDate(int month, int day) {
		this.month = month;
		this.day = day;
	}

	/**
	 * Compares two ADate objects.
	 * Note that there is no year, so this comparison assumes comparing within a single calendar year
	 * i.e. 6/8/2019 is before 3/2/2020, but compareDate(ADate date) will return -1
	 * @return -1 if the date is earlier than the argument, 1 if the date is later than the argument, 0 if the ADate objects are equal in value
	 */
	@Override
	public int compareDate(ADate date) {
		if (this.month > date.getMonth()) {
			return 1;
		} else if (this.month < date.getMonth()) {
			return -1;
		}
		// Execution here means that the months are equal
		if (this.day > date.getDay()) {
			return 1;
		} else if (this.day < date.getDay()) {
			return -1;
		}
		return 0;
	}

	@Override
	public int getMonth() {
		return month;
	}

	@Override
	public int getDay() {
		return day;
	}

	@Override
	public String toString() {
		return month + "/" + day;
	}
	
}
