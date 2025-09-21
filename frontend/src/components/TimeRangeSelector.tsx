
export type TimeRange = '24h' | '7d' | '30d' | 'custom';

interface TimeRangeSelectorProps {
    selectedRange: TimeRange;
    onRangeChange: (range: TimeRange) => void;
    customStartDate?: string;
    customEndDate?: string;
    onCustomDateChange?: (startDate: string, endDate: string) => void;
}

export const TimeRangeSelector = ({
    selectedRange,
    onRangeChange,
    customStartDate,
    customEndDate,
    onCustomDateChange,
}: TimeRangeSelectorProps) => {
    const ranges = [
        { value: '24h', label: 'Последние 24 часа' },
        { value: '7d', label: 'Последние 7 дней' },
        { value: '30d', label: 'Последние 30 дней' },
        { value: 'custom', label: 'Произвольный период' },
    ] as const;

    const handleCustomDateChange = (field: 'start' | 'end', value: string) => {
        if (onCustomDateChange) {
            if (field === 'start') {
                onCustomDateChange(value, customEndDate || '');
            } else {
                onCustomDateChange(customStartDate || '', value);
            }
        }
    };

    return (
        <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm p-4">
            <div className="flex flex-col sm:flex-row sm:items-center gap-4">
                <div className="flex flex-wrap gap-2">
                    {ranges.map((range) => (
                        <button
                            key={range.value}
                            onClick={() => onRangeChange(range.value)}
                            className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${selectedRange === range.value
                                ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600'
                                }`}
                        >
                            {range.label}
                        </button>
                    ))}
                </div>

                {selectedRange === 'custom' && (
                    <div className="flex flex-col sm:flex-row gap-2">
                        <div className="flex flex-col">
                            <label className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">
                                Начало периода
                            </label>
                            <input
                                type="datetime-local"
                                value={customStartDate || ''}
                                onChange={(e) => handleCustomDateChange('start', e.target.value)}
                                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>
                        <div className="flex flex-col">
                            <label className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">
                                Конец периода
                            </label>
                            <input
                                type="datetime-local"
                                value={customEndDate || ''}
                                onChange={(e) => handleCustomDateChange('end', e.target.value)}
                                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};
